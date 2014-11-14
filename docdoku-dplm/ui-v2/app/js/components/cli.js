angular.module('dplm.services.cli',[])

.service('CliService',function(ConfigurationService, NotificationService,$q,$log){

        var configuration = ConfigurationService.configuration;
        var classPath = process.cwd() + '/cli/docdoku-cli-jar-with-dependencies.jar';
        var mainClass = 'com.docdoku.cli.MainCommand';
        var memOptions = '-Xmx1024M';

        var  run = function(args,progressHandler){

            return $q(function(reject,resolve){

                $log.log(([memOptions,'-cp',classPath, mainClass].concat(args)).join(' '));

                var spawn = require('child_process').spawn;
                var cliProcess  = spawn('java', [memOptions,'-cp',classPath, mainClass].concat(args));

                var objects = [];
                var errors = [];

                cliProcess.stdout.on('data',function(data){
                    var entries = data.toString().split('\n');
                    angular.forEach(entries,function(entry){
                        if(entry && entry.trim()){
                            objects.push(JSON.parse(entry));
                            NotificationService.toast(entry);
                        }
                    });
                });

                cliProcess.stderr.on('data',function(data){
                    var entries = data.toString().split('\n');
                    angular.forEach(entries,function(entry){
                        if(entry && entry.trim()){
                            errors.push(JSON.parse(entry));
                            NotificationService.toast(entry);
                        }
                    });
                });

                cliProcess.stderr.on('close', function (code, signal) {
                    if(!code){
                        var result = objects.length ? objects[objects.length-1] : null;
                        resolve(result);
                    }else{
                        reject();
                    }
                });

            });

        };
        
        this.checkRequirements = function(){

            $log.info('Checking requirements');

            return $q(function(resolve,reject){

                var spawn = require('child_process').spawn('java', ['-version']);

                spawn.on('error', function(err){
                    $log.log('spawn.onerror')
                    return reject('No java found');
                });

                spawn.stderr.on('data', function(data) {
                    data = data.toString().split('\n')[0];
                    var javaVersion = new RegExp('java version').test(data) ? data.split(' ')[2].replace(/"/g, '') : false;
                    if (javaVersion != false && javaVersion >= '1.7') {
                        $log.log('javaVersion'+javaVersion)
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
                file
            ];

            return run(args);
        };

        this.getStatusForPart= function (part) {

            var args = [
                'st',
                '-F', 'json',
                '-h', configuration.host,
                '-P', configuration.port,
                '-u', configuration.user,
                '-p', configuration.password,
                '-w', part.getWorkspace(),
                '-o', part.getNumber(),
                '-r', part.getVersion()
            ];

            return run(args);

        };

        this.checkout= function (part, options) {

            var args = [
                'co',
                '-F', 'json',
                '-h', configuration.host,
                '-P', configuration.port,
                '-u', configuration.user,
                '-p', configuration.password,
                '-w', part.getWorkspace(),
                '-o', part.getNumber(),
                '-r', part.getVersion()
            ];

            if (options.recursive) {
                args.push('-R');
            }
            if (options.force) {
                args.push('f');
            }
            if (options.baseline) {
                args.push('-b');
                args.push(options.baseline);
            }

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
                '-w', part.getWorkspace(),
                '-o', part.getNumber(),
                '-r', part.getVersion()
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
                '-w', part.getWorkspace(),
                '-o', part.getNumber(),
                '-r', part.getVersion()
            ];

            return run(args);
        };

        this.download= function (part, options) {

            var args = [
                'get',
                '-F', 'json',
                '-h', configuration.host,
                '-P', configuration.port,
                '-u', configuration.user,
                '-p', configuration.password,
                '-w', part.getWorkspace(),
                '-o', part.getNumber(),
                '-r', part.getVersion()
            ];

            if (options.recursive) {
                args.push('-R');
            }
            if (options.force) {
                args.push('f');
            }
            if (options.baseline) {
                args.push('-b');
                args.push(options.baseline);
            }

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
                '-w', part.getWorkspace(),
                '-o', part.getNumber()
            ];

            if (part.getName()) {
                args.push('-N');
                args.push(part.getName());
            }
            if (part.getDescription()) {
                args.push('-d');
                args.push(part.getDescription());
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

        this.getPartMasters= function (workspace, start, max) {

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
                '-w', part.getWorkspace(),
                '-o', part.getNumber(),
                '-r', part.getVersion()
            ];

            return run(args);
        }
       

})