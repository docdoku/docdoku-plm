(function(){

    'use strict';

    angular.module('dplm.services.cli', [])

        .service('CliService', function (ConfigurationService, NotificationService, $q, $log) {

            var configuration = ConfigurationService.configuration;
            var classPath = process.cwd() + '/docdoku-cli-jar-with-dependencies.jar';
            var mainClass = 'com.docdoku.cli.MainCommand';
            var memOptions = '-Xmx1024M';

            var run = function (args, silent) {

                var deferred = $q.defer();

                $log.info(([memOptions, '-cp', classPath, mainClass].concat(args)).join(' '));

                var spawn = require('child_process').spawn;
                var cliProcess = spawn(configuration.java ||'java', [memOptions, '-cp', classPath, mainClass].concat(args));

                var objects = [];
                var errors = [];

                cliProcess.stdout.on('data', function (data) {
                    $log.log(data.toString());
                    var entries = data.toString().split('\n');
                    angular.forEach(entries, function (entry) {
                        if (entry && entry.trim()) {
                            var object = JSON.parse(entry);
                            if (object.progress) {
                                deferred.notify(object.progress);
                            } else if (object.info) {
                                if (!silent) {
                                    NotificationService.toast(object.info);
                                }
                                console.info(object.info);
                            }
                            objects.push(object);
                        }
                    });
                });

                cliProcess.stderr.on('data', function (data) {
                    $log.warn('STDERR ' + data.toString());
                    var entries = data.toString().split('\n');
                    angular.forEach(entries, function (entry) {
                        if (entry && entry.trim()) {
                            var object = JSON.parse(entry);
                            if (object.error) {
                                if (!silent) {
                                    NotificationService.toast(object.error);
                                }
                            }
                            errors.push(object);
                        }
                    });
                });

                cliProcess.stderr.on('close', function () {
                    if (objects.length) {
                        deferred.resolve(objects[objects.length - 1]);
                    }
                    else if (errors.length) {
                        deferred.reject(errors[errors.length - 1]);
                    } else {
                        deferred.resolve();
                    }
                });

                return deferred.promise;

            };

            this.getWorkspaces = function () {
                var args = [
                    'wl',
                    '-F', 'json',
                    '-h', configuration.host,
                    '-P', configuration.port,
                    '-u', configuration.user,
                    '-p', configuration.password
                ];

                return run(args);

            };

            this.getPartStatusForFile = function (file) {

                var args = [
                    'st', 'part',
                    '-F', 'json',
                    '-h', configuration.host,
                    '-P', configuration.port,
                    '-u', configuration.user,
                    '-p', configuration.password,
                    file.path
                ];

                return run(args, true).then(function (status) {
                    if (!file.part) {
                        file.part = {};
                    }
                    angular.extend(file.part, status);
                });
            };

            this.getStatusForPart = function (part) {

                var args = [
                    'st', 'part',
                    '-F', 'json',
                    '-h', configuration.host,
                    '-P', configuration.port,
                    '-u', configuration.user,
                    '-p', configuration.password,
                    '-w', part.workspace,
                    '-o', part.partNumber,
                    '-r', part.version
                ];

                return run(args, true).then(function (newPart) {
                    angular.extend(part, newPart);
                });

            };

            this.checkoutPart = function (part, path, options) {

                var args = [
                    'co', 'part',
                    '-F', 'json',
                    '-h', configuration.host,
                    '-P', configuration.port,
                    '-u', configuration.user,
                    '-p', configuration.password,
                    '-w', part.workspace,
                    '-o', part.partNumber,
                    '-r', part.version
                ];

                if (options.recursive) {
                    args.push('-R');
                }
                if (options.force) {
                    args.push('-f');
                }
                if (options.baseline) {
                    args.push('-b');
                    args.push(options.baseline);
                }

                args.push(path);

                return run(args);

            };

            this.checkin = function (part,path) {

                var args = [
                    'ci',
                    '-F', 'json',
                    '-h', configuration.host,
                    '-P', configuration.port,
                    '-u', configuration.user,
                    '-p', configuration.password,
                    '-w', part.workspace,
                    '-o', part.partNumber,
                    '-r', part.version
                ];

                if(path){
                    args.push(path);
                }

                return run(args);

            };

            this.undoCheckout = function (part) {

                var args = [
                    'uco',
                    '-F', 'json',
                    '-h', configuration.host,
                    '-P', configuration.port,
                    '-u', configuration.user,
                    '-p', configuration.password,
                    '-w', part.workspace,
                    '-o', part.partNumber,
                    '-r', part.version
                ];

                return run(args);
            };

            this.downloadNativeCad = function (part, path, options) {

                var args = [
                    'get', 'part',
                    '-F', 'json',
                    '-h', configuration.host,
                    '-P', configuration.port,
                    '-u', configuration.user,
                    '-p', configuration.password,
                    '-w', part.workspace,
                    '-o', part.partNumber,
                    '-r', part.version
                ];

                if (options.recursive) {
                    args.push('-R');
                }
                if (options.force) {
                    args.push('-f');
                }
                if (options.baseline) {
                    args.push('-b');
                    args.push(options.baseline);
                }

                args.push(path);

                return run(args);

            };

            this.putCADFile = function (workspace, file) {

                var args = [
                    'put', 'part',
                    '-F', 'json',
                    '-h', configuration.host,
                    '-P', configuration.port,
                    '-u', configuration.user,
                    '-p', configuration.password,
                    '-w', workspace
                ];

                args.push(file);

                return run(args);

            };
            this.createPart = function (part, filePath) {

                var args = [
                    'cr',
                    '-F', 'json',
                    '-h', configuration.host,
                    '-P', configuration.port,
                    '-u', configuration.user,
                    '-p', configuration.password,
                    '-w', part.workspace,
                    '-o', part.partNumber
                ];

                if (part.name) {
                    args.push('-N');
                    args.push(part.name);
                }
                if (part.description) {
                    args.push('-d');
                    args.push(part.description);
                }

                if (part.standard) {
                    args.push('-s');
                }

                args.push(filePath);

                return run(args);

            };

            this.getPartMastersCount = function (workspace) {

                var args = [
                    'pl',
                    '-F', 'json',
                    '-h', configuration.host,
                    '-P', configuration.port,
                    '-u', configuration.user,
                    '-p', configuration.password,
                    '-w', workspace,
                    '-c'
                ];

                return run(args);

            };

            this.getPartMasters = function (workspace, start, max) {

                var args = [
                    'pl',
                    '-F', 'json',
                    '-h', configuration.host,
                    '-P', configuration.port,
                    '-u', configuration.user,
                    '-p', configuration.password,
                    '-s', Number(start).toString(),
                    '-m', Number(max).toString(),
                    '-w', workspace
                ];

                return run(args);

            };

            this.searchPartMasters = function (workspace, search) {

                var args = [
                    's',
                    '-F', 'json',
                    '-h', configuration.host,
                    '-P', configuration.port,
                    '-u', configuration.user,
                    '-p', configuration.password,
                    '-w', workspace,
                    '-s', search
                ];

                return run(args);
            };

            this.getBaselines = function (part) {
                var args = [
                    'bl',
                    '-F', 'json',
                    '-h', configuration.host,
                    '-P', configuration.port,
                    '-u', configuration.user,
                    '-p', configuration.password,
                    '-w', part.workspace,
                    '-o', part.partNumber,
                    '-r', part.version
                ];

                return run(args);
            };

            this.getConversionStatus = function (part) {

                var args = [
                    'cv',
                    '-F', 'json',
                    '-h', configuration.host,
                    '-P', configuration.port,
                    '-u', configuration.user,
                    '-p', configuration.password,
                    '-w', part.workspace,
                    '-o', part.partNumber,
                    '-r', part.version,
                    '-i', part.iterations[part.iterations.length-1]
                ];

                return run(args);

            };

            this.fetchAccount = function(){

                var args = [
                    'a',
                    '-F', 'json',
                    '-h', configuration.host,
                    '-P', configuration.port,
                    '-u', configuration.user,
                    '-p', configuration.password
                ];

                return run(args);
            }

        });
})();