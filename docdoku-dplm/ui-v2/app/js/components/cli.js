angular.module('dplm.services.cli',[])
.service('CliService',function(Configuration){

        var configuration = ConfigurationService.configuration;
        var classPath = process.cwd() + '/dplm/docdoku-cli-jar-with-dependencies.jar';
        var mainClass = com.docdoku.cli.MainCommand;
        var memOptions = '-Xmx1024M';

        this.checkRequirements = function(){

            function javaversion(callback) {

                var spawn = require('child_process').spawn('java', ['-version']);

                spawn.on('error', function(err){
                    return callback(err, null);
                })
                spawn.stderr.on('data', function(data) {
                    data = data.toString().split('\n')[0];
                    var javaVersion = new RegExp('java version').test(data) ? data.split(' ')[2].replace(/"/g, '') : false;
                    if (javaVersion != false) {
                        console.log('TODO: We have Java installed ' + javaVersion);

                    } else {
                        console.log('No java found');
                    }
                });

            }

            javaversion(function(err,version){
                console.log("Version is " + version);
            });

        };

        this.run=function(args,progressHandler){

            return $q(function(reject,resolve){

                var spawn = require('child_process').spawn;
                var cliProcess  = spawn('java', [memOptions,'-cp',classPath, mainClass].concat(args));

                cliProcess.stdout.on('data',function(err,data){
                    console.log('stdout : ' + data.toString());
                    // progressHandler
                });
                cliProcess.stderr.on('data',function(err,data){
                    console.error('stderr : ' + data.toString());
                });
                cliProcess.stderr.on('close', function (code, signal) {
                    console.log('child process terminated due to receipt of signal '+signal);
                    resolve();
                });

            });

        };

        this.getWorkspaces = function (callbacks) {
            var args = [
                'wl',
                '-h', configuration.host,
                '-P', configuration.port,
                '-u', configuration.user,
                '-p', configuration.password
            ];

            return this.run(args,callbacks);

        };

        this.getStatusForFile= function (file, callbacks) {

            var args = [
                'st',
                '-h', configuration.host,
                '-P', configuration.port,
                '-u', configuration.user,
                '-p', configuration.password,
                file
            ];

            return this.run(args,callbacks);
        };

        this.getStatusForPart= function (part, callbacks) {

            var args = [
                'st',
                '-h', configuration.host,
                '-P', configuration.port,
                '-u', configuration.user,
                '-p', configuration.password,
                '-w', part.getWorkspace(),
                '-o', part.getNumber(),
                '-r', part.getVersion()
            ];

            return this.run(args,callbacks);

        };

        this.checkout= function (part, options, callbacks) {

            var args = [
                'co',
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

            return this.run(args,callbacks);

        };

        this.checkin= function (part, callbacks) {

            var args = [
                'ci',
                '-h', configuration.host,
                '-P', configuration.port,
                '-u', configuration.user,
                '-p', configuration.password,
                '-w', part.getWorkspace(),
                '-o', part.getNumber(),
                '-r', part.getVersion()
            ];

            return this.run(args,callbacks);

        };

        this.undoCheckout= function (part, callbacks) {

            var args = [
                'uco',
                '-h', configuration.host,
                '-P', configuration.port,
                '-u', configuration.user,
                '-p', configuration.password,
                '-w', part.getWorkspace(),
                '-o', part.getNumber(),
                '-r', part.getVersion()
            ];

            return this.run(args,callbacks);
        };

        this.download= function (part, options, callbacks) {

            var args = [
                'get',
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

            return this.run(args,callbacks);

        };

        this.createPart= function (part, filePath, callbacks) {

            var args = [
                'cr',
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

            return this.run(args,callbacks);

        };

        this.getPartMastersCount= function (workspace, callbacks) {

            var args = [
                'pl',
                '-h', configuration.host,
                '-P', configuration.port,
                '-u', configuration.user,
                '-p', configuration.password,
                '-w', workspace,
                '-c'
            ];

            return this.run(args,callbacks);

        };

        this.getPartMasters= function (workspace, start, max, callbacks) {

            var args = [
                'pl',
                '-h', configuration.host,
                '-P', configuration.port,
                '-u', configuration.user,
                '-p', configuration.password,
                '-s', Number(start).toString(),
                '-m', Number(max).toString(),
                '-w', workspace
            ];

            return this.run(args,callbacks);

        };

        this.searchPartMasters= function (workspace, search, callbacks) {

            var args = [
                's',
                '-h', configuration.host,
                '-P', configuration.port,
                '-u', configuration.user,
                '-p', configuration.password,
                '-w', workspace,
                '-s', search
            ];

            return this.run(args,callbacks);
        };

        this.getBaselines= function (part, callbacks) {
            var args = [
                'bl',
                '-h', configuration.host,
                '-P', configuration.port,
                '-u', configuration.user,
                '-p', configuration.password,
                '-w', part.getWorkspace(),
                '-o', part.getNumber(),
                '-r', part.getVersion()
            ];

            return this.run(args,callbacks);
        }
       

})