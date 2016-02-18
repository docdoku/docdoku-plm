(function(){

    'use strict';

    angular.module('dplm.services.cli', [])

        .service('CliService', function (ConfigurationService, NotificationService, $q, $log) {

            var configuration = ConfigurationService.configuration;
            var classPath = process.cwd() + '/docdoku-cli-jar-with-dependencies.jar';
            var mainClass = 'com.docdoku.cli.MainCommand';
            var memOptions = '-Xmx1024M';

            var run = function (args, silent, onOutput) {

                if(configuration.ssl){
                    args.push('--ssl');
                }

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

                                if(typeof onOutput === 'function'){
                                    onOutput(object);
                                }

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

                            if(typeof onOutput === 'function'){
                                onOutput(object);
                            }

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

            //////////////////////////////////////////////
            // Common services

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
            };

            this.getStatusForFile = function (file) {

                var args = [
                    'st',
                    '-F', 'json',
                    '-h', configuration.host,
                    '-P', configuration.port,
                    '-u', configuration.user,
                    '-p', configuration.password,
                    file.path
                ];

                return run(args, true).then(function (status) {

                    if(status.id){
                        if (!file.document) {
                            file.document = {};
                        }
                        angular.extend(file.document, status);
                    }else if(status.partNumber){
                        if (!file.part) {
                            file.part = {};
                        }
                        angular.extend(file.part, status);
                    }

                });
            };


            //////////////////////////////////////////////
            // Part services


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

            this.checkoutPart = function (part, path, options, onOutput) {

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

                return run(args, true, onOutput);

            };

            this.checkinPart = function (part,options, onOutput) {

                var args = [
                    'ci', 'part',
                    '-F', 'json',
                    '-h', configuration.host,
                    '-P', configuration.port,
                    '-u', configuration.user,
                    '-p', configuration.password,
                    '-w', part.workspace,
                    '-o', part.partNumber,
                    '-r', part.version
                ];

                if(options && options.message){
                    args.push('-m');
                    args.push(options.message);
                }

                if(options && options.path){
                    args.push(options.path);
                }

                return run(args,true,onOutput);

            };

            this.undoCheckoutPart = function (part, onOutput) {

                var args = [
                    'uco', 'part',
                    '-F', 'json',
                    '-h', configuration.host,
                    '-P', configuration.port,
                    '-u', configuration.user,
                    '-p', configuration.password,
                    '-w', part.workspace,
                    '-o', part.partNumber,
                    '-r', part.version
                ];

                return run(args, true, onOutput);
            };

            this.downloadNativeCad = function (part, path, options, cbOutput) {

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

                return run(args, true, cbOutput);

            };

            this.putCADFile = function (workspace, file, onOutput) {

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

                return run(args, true, onOutput);

            };

            this.createPart = function (part, filePath, onOutput) {

                var args = [
                    'cr', 'part',
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

                return run(args,true,onOutput);

            };

            this.getPartMastersCount = function (workspace) {

                var args = [
                    'l', 'part',
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
                    'l', 'part',
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
                    's', 'part',
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

            this.getConversionStatus = function (part, onOutput) {

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

                return run(args,true,onOutput);

            };


            //////////////////////////////////////////////
            // Document services

            this.getFolders=function(workspace,folder){
                var args = [
                    'f',
                    '-F', 'json',
                    '-h', configuration.host,
                    '-P', configuration.port,
                    '-u', configuration.user,
                    '-p', configuration.password,
                    '-w', workspace
                ];

                if(folder){
                    args.push('-f');
                    args.push(folder);
                }

                return run(args);
            };

            this.getDocumentsRevisionsInFolder=function(workspace,folder){
                var args = [
                    'l', 'document',
                    '-F', 'json',
                    '-h', configuration.host,
                    '-P', configuration.port,
                    '-u', configuration.user,
                    '-p', configuration.password,
                    '-w', workspace
                ];

                if(folder){
                    args.push('-f');
                    args.push(folder);
                }

                return run(args);
            };

            this.getCheckedOutDocumentsRevisions=function(workspace){
                var args = [
                    'l', 'document',
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

            this.downloadDocumentFiles=function(document,path,options,onOutput){

                var args = [
                    'get', 'document',
                    '-F', 'json',
                    '-h', configuration.host,
                    '-P', configuration.port,
                    '-u', configuration.user,
                    '-p', configuration.password,
                    '-w', document.workspace,
                    '-o', document.id,
                    '-r', document.version
                ];

                if (options.force) {
                    args.push('-f');
                }

                args.push(path);

                return run(args,true,onOutput);
            };

            this.getStatusForDocument = function(document) {

                var args = [
                    'st', 'document',
                    '-F', 'json',
                    '-h', configuration.host,
                    '-P', configuration.port,
                    '-u', configuration.user,
                    '-p', configuration.password,
                    '-w', document.workspace,
                    '-o', document.id,
                    '-r', document.version
                ];

                return run(args, true).then(function (newDocument) {
                    angular.extend(document,newDocument);
                });

            };

            this.checkoutDocument = function (document, path, options, onOutput) {

                var args = [
                    'co', 'document',
                    '-F', 'json',
                    '-h', configuration.host,
                    '-P', configuration.port,
                    '-u', configuration.user,
                    '-p', configuration.password,
                    '-w', document.workspace,
                    '-o', document.id,
                    '-r', document.version
                ];

                if (options.force) {
                    args.push('-f');
                }

                args.push(path);

                return run(args, true, onOutput);

            };

            this.checkinDocument = function (document, options, onOutput) {

                var args = [
                    'ci', 'document',
                    '-F', 'json',
                    '-h', configuration.host,
                    '-P', configuration.port,
                    '-u', configuration.user,
                    '-p', configuration.password,
                    '-w', document.workspace,
                    '-o', document.id,
                    '-r', document.version
                ];

                if(options && options.message){
                    args.push('-m');
                    args.push(options.message);
                }

                if(options && options.path){
                    args.push(options.path);
                }

                return run(args, true, onOutput);

            };

            this.undoCheckoutDocument = function (document, onOutput) {

                var args = [
                    'uco', 'document',
                    '-F', 'json',
                    '-h', configuration.host,
                    '-P', configuration.port,
                    '-u', configuration.user,
                    '-p', configuration.password,
                    '-w', document.workspace,
                    '-o', document.id,
                    '-r', document.version
                ];

                return run(args, true, onOutput);
            };

            this.putDocumentFile = function (workspace, file, onOutput) {

                var args = [
                    'put', 'document',
                    '-F', 'json',
                    '-h', configuration.host,
                    '-P', configuration.port,
                    '-u', configuration.user,
                    '-p', configuration.password,
                    '-w', workspace
                ];

                args.push(file);

                return run(args, true, onOutput);

            };

            this.createDocument = function (document, filePath, onOutput) {

                var args = [
                    'cr', 'document',
                    '-F', 'json',
                    '-h', configuration.host,
                    '-P', configuration.port,
                    '-u', configuration.user,
                    '-p', configuration.password,
                    '-w', document.workspace,
                    '-o', document.id
                ];

                if (document.title) {
                    args.push('-N');
                    args.push(document.title);
                }
                if (document.description) {
                    args.push('-d');
                    args.push(document.description);
                }

                args.push(filePath);

                return run(args, true, onOutput);

            };


        });
})();