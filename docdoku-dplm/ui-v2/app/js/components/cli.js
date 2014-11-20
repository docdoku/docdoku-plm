'use strict';

angular.module('dplm.services.cli',[])

.service('CliService',function(ConfigurationService, NotificationService,$q,$log){

    var configuration = ConfigurationService.configuration;
    var classPath = process.cwd() + '/cli/docdoku-cli-jar-with-dependencies.jar';
    var mainClass = 'com.docdoku.cli.MainCommand';
    var memOptions = '-Xmx1024M';

    this.requirementsError = 'CLI_REQUIREMEMENTS_ERROR';

    var  run = function(args,silent){

        var deferred = $q.defer();

        $log.info(([memOptions,'-cp',classPath, mainClass].concat(args)).join(' '));

        var spawn = require('child_process').spawn;
        var cliProcess  = spawn('java', [memOptions,'-cp',classPath, mainClass].concat(args));

        var objects = [];
        var errors = [];

        cliProcess.stdout.on('data',function(data){
            $log.log(data.toString());
            var entries = data.toString().split('\n');
            angular.forEach(entries,function(entry){
                if(entry && entry.trim()){
                    var object = JSON.parse(entry);
                    if(object.progress){
                        deferred.notify(object.progress);
                    }else if(object.info){
                        if(!silent){
                            NotificationService.toast(object.info);
                        }
                        console.info(object.info);
                    }
                    objects.push(object);
                }
            });
        });

        cliProcess.stderr.on('data',function(data){
            $log.warn('STDERR '+data.toString());
            var entries = data.toString().split('\n');
            angular.forEach(entries,function(entry){
                if(entry && entry.trim()){
                    var object = JSON.parse(entry);
                    if(object.error){
                        if(!silent){
                            NotificationService.toast(object.error);
                        }
                    }
                    errors.push(object);
                }
            });
        });

        cliProcess.stderr.on('close', function () {      
            if(objects.length){
                deferred.resolve(objects[objects.length-1]);
            }
            else if (errors.length){                    
                deferred.reject(errors[errors.length-1]);
            }else{
                deferred.resolve();
            }
        });

        return deferred.promise;

    };
    
    this.checkRequirements = function(){

        $log.info('Checking requirements');

        return $q(function(resolve,reject){

            var spawn = require('child_process').spawn('java', ['-version']);

            spawn.on('error', function(err){
                $log.log('spawn.onerror');
                return reject('No java found');
            });

            spawn.stderr.on('data', function(data) {
                data = data.toString().split('\n')[0];
                var javaVersion = new RegExp('java version').test(data) ? data.split(' ')[2].replace(/"/g, '') : false;
                if (javaVersion && javaVersion >= '1.7') {
                    $log.log('javaVersion'+javaVersion);
                    resolve(javaVersion);
                } else {
                    reject('No java found or version not matching');
                }
            });

        });

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

    this.getStatusForFile= function (file) {

        var args = [
        'st',
        '-F', 'json',
        '-h', configuration.host,
        '-P', configuration.port,
        '-u', configuration.user,
        '-p', configuration.password,
        file.path
        ];

        return run(args,true).then(function(status){
            if(!file.part){
                file.part ={};
            }
            angular.extend(file.part,status);
        });
    };

    this.getStatusForPart= function (part) {

        var args = [
        'st',
        '-F', 'json',
        '-h', configuration.host,
        '-P', configuration.port,
        '-u', configuration.user,
        '-p', configuration.password,
        '-w', part.workspace,
        '-o', part.partNumber,
        '-r', part.version
        ];

        return run(args,true).then(function(newPart){               
            angular.extend(part,newPart);
        });

    };

    this.checkout= function (part, path, options) {

        var args = [
        'co',
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

    this.checkin= function (part) {

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

        return run(args);

    };

    this.undoCheckout= function (part) {

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

    this.download = function (part, path, options) {

        var args = [
        'get',
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

    this.put = function (workspace, file) {

        var args = [
        'put',
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
    this.createPart= function (part, filePath) {

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

    this.getPartMastersCount= function (workspace) {

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

    this.searchPartMasters= function (workspace, search) {

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

    this.getBaselines= function (part) {
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
    

});