define(function(){

    var escapeShell = function(cmd) {
        return '"'+cmd+'"';
    };

    var WindowsCMD = {
        base:function(){
            return '"'+APP_GLOBAL.GLOBAL_CONF.javaHome+'\\bin\\java" -Xmx1024M -cp "' + classPath + '" com.docdoku.cli.NodeCommand ';
        }
    };

    var LinuxCMD = {
        base:function(){
            return'"'+APP_GLOBAL.GLOBAL_CONF.javaHome+'/bin/java" -Xmx1024M -cp "' + classPath + '" com.docdoku.cli.NodeCommand ';
        }
    };

    var OsxCMD = {
        base:function(){
            return '"'+APP_GLOBAL.GLOBAL_CONF.javaHome+'/bin/java" -Xmx1024M -cp "' + classPath + '" com.docdoku.cli.NodeCommand ';
        }
    };

    var toCommand = function(args){
        var command = "";
        _(args).each(function(arg){
            command += escapeShell(arg) + " ";
        });
        return command;
    };

    var Dplm = {};

    switch(os.type()){
        case "Windows_NT" : _.extend(Dplm,WindowsCMD); break;
        case "Linux" : _.extend(Dplm,LinuxCMD); break;
        case "Darwin" : _.extend(Dplm,OsxCMD); break;
        default :_.extend(Dplm,LinuxCMD); break;
    }

    _.extend(Dplm,{

        run:function(args,callbacks){
            var command = toCommand(args);
            console.log(command);
            exec(Dplm.base() + command,{ maxBuffer: 1274916 * 2  },function (error, stdout, stderr) {
                var results = stdout ? JSON.parse(stdout) : {};
                if (error || stderr) {
                    callbacks.error();
                } else {
                    callbacks.success(results);
                }
            });
        },

        getWorkspaces: function (callbacks) {

            var args = [
                "wl",
                "-h", APP_GLOBAL.GLOBAL_CONF.host,
                "-P", APP_GLOBAL.GLOBAL_CONF.port,
                "-u", APP_GLOBAL.GLOBAL_CONF.user,
                "-p", APP_GLOBAL.GLOBAL_CONF.password
            ];

            Dplm.run(args,callbacks);

        },

        getStatusForFile: function (file, callbacks) {

            var args = [
                "st",
                "-h", APP_GLOBAL.GLOBAL_CONF.host,
                "-P", APP_GLOBAL.GLOBAL_CONF.port,
                "-u", APP_GLOBAL.GLOBAL_CONF.user,
                "-p", APP_GLOBAL.GLOBAL_CONF.password,
                file
            ];

            Dplm.run(args,callbacks);
        },

        getStatusForPart: function (part, callbacks) {

            var args = [
                "st",
                "-h", APP_GLOBAL.GLOBAL_CONF.host,
                "-P", APP_GLOBAL.GLOBAL_CONF.port,
                "-u", APP_GLOBAL.GLOBAL_CONF.user,
                "-p", APP_GLOBAL.GLOBAL_CONF.password,
                "-w", part.getWorkspace(),
                "-o", part.getNumber(),
                "-r", part.getVersion()
            ];

            Dplm.run(args,callbacks);

        },

        checkout: function (part, options, callbacks) {

            var args = [
                "co",
                "-h", APP_GLOBAL.GLOBAL_CONF.host,
                "-P", APP_GLOBAL.GLOBAL_CONF.port,
                "-u", APP_GLOBAL.GLOBAL_CONF.user,
                "-p", APP_GLOBAL.GLOBAL_CONF.password,
                "-w", part.getWorkspace(),
                "-o", part.getNumber(),
                "-r", part.getVersion()
            ];

            if (options.recursive) {
                args.push("-R");
            }
            if (options.force) {
                args.push("f");
            }
            if (options.baseline) {
                args.push("-b");
                args.push(options.baseline);
            }

            Dplm.run(args,callbacks);

        },

        checkin: function (part, callbacks) {

            var args = [
                "ci",
                "-h", APP_GLOBAL.GLOBAL_CONF.host,
                "-P", APP_GLOBAL.GLOBAL_CONF.port,
                "-u", APP_GLOBAL.GLOBAL_CONF.user,
                "-p", APP_GLOBAL.GLOBAL_CONF.password,
                "-w", part.getWorkspace(),
                "-o", part.getNumber(),
                "-r", part.getVersion()
            ];

            Dplm.run(args,callbacks);

        },

        undoCheckout: function (part, callbacks) {

            var args = [
                "uco",
                "-h", APP_GLOBAL.GLOBAL_CONF.host,
                "-P", APP_GLOBAL.GLOBAL_CONF.port,
                "-u", APP_GLOBAL.GLOBAL_CONF.user,
                "-p", APP_GLOBAL.GLOBAL_CONF.password,
                "-w", part.getWorkspace(),
                "-o", part.getNumber(),
                "-r", part.getVersion()
            ];

            Dplm.run(args,callbacks);
        },

        download: function (part, options, callbacks) {

            var args = [
                "get",
                "-h", APP_GLOBAL.GLOBAL_CONF.host,
                "-P", APP_GLOBAL.GLOBAL_CONF.port,
                "-u", APP_GLOBAL.GLOBAL_CONF.user,
                "-p", APP_GLOBAL.GLOBAL_CONF.password,
                "-w", part.getWorkspace(),
                "-o", part.getNumber(),
                "-r", part.getVersion()
            ];

            if (options.recursive) {
                args.push("-R");
            }
            if (options.force) {
                args.push("f");
            }
            if (options.baseline) {
                args.push("-b");
                args.push(options.baseline);
            }

            Dplm.run(args,callbacks);

        },

        createPart: function (part, filePath, callbacks) {

            var args = [
                "cr",
                "-h", APP_GLOBAL.GLOBAL_CONF.host,
                "-P", APP_GLOBAL.GLOBAL_CONF.port,
                "-u", APP_GLOBAL.GLOBAL_CONF.user,
                "-p", APP_GLOBAL.GLOBAL_CONF.password,
                "-w", part.getWorkspace(),
                "-o", part.getNumber()
            ];

            if (part.getName()) {
                args.push("-N");
                args.push(part.getName());
            }
            if (part.getDescription()) {
                args.push("-d");
                args.push(part.getDescription());
            }

            args.push(filePath);

            Dplm.run(args,callbacks);

        },

        getPartMastersCount: function (workspace, callbacks) {

            var args = [
                "pl",
                "-h", APP_GLOBAL.GLOBAL_CONF.host,
                "-P", APP_GLOBAL.GLOBAL_CONF.port,
                "-u", APP_GLOBAL.GLOBAL_CONF.user,
                "-p", APP_GLOBAL.GLOBAL_CONF.password,
                "-w", workspace,
                "-c"
            ];

            Dplm.run(args,callbacks);

        },

        getPartMasters: function (workspace, start, max, callbacks) {

            var args = [
                "pl",
                "-h", APP_GLOBAL.GLOBAL_CONF.host,
                "-P", APP_GLOBAL.GLOBAL_CONF.port,
                "-u", APP_GLOBAL.GLOBAL_CONF.user,
                "-p", APP_GLOBAL.GLOBAL_CONF.password,
                "-s", Number(start).toString(),
                "-m", Number(max).toString(),
                "-w", workspace
            ];

            Dplm.run(args,callbacks);

        },

        searchPartMasters: function (workspace, search, callbacks) {

            var args = [
                "s",
                "-h", APP_GLOBAL.GLOBAL_CONF.host,
                "-P", APP_GLOBAL.GLOBAL_CONF.port,
                "-u", APP_GLOBAL.GLOBAL_CONF.user,
                "-p", APP_GLOBAL.GLOBAL_CONF.password,
                "-w", workspace,
                "-s", search
            ];

            Dplm.run(args,callbacks);
        },

        getBaselines: function (part, callbacks) {

            var args = [
                "bl",
                "-h", APP_GLOBAL.GLOBAL_CONF.host,
                "-P", APP_GLOBAL.GLOBAL_CONF.port,
                "-u", APP_GLOBAL.GLOBAL_CONF.user,
                "-p", APP_GLOBAL.GLOBAL_CONF.password,
                "-w", part.getWorkspace(),
                "-o", part.getNumber(),
                "-r", part.getVersion()
            ];

            Dplm.run(args,callbacks);

        }
    });

    return Dplm;
});