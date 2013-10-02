define(function(){

    var java = requireNode("java");
    java.classpath.push(classPath);

    var Dplm = {

        run:function(args,callbacks){
            java.callStaticMethod("com.docdoku.cli.MainCommand", "nodeMain", args, function (err, jsonData) {
                if (err) {
                    callbacks.error(err);
                } else {
                    var results = jsonData ? JSON.parse(jsonData) : {};
                    if(results.error){
                        callbacks.error(results);
                    }else{
                        callbacks.success(results);
                    }
                }
            });
        },

        getWorkspaces: function (callbacks) {

            var args = java.newArray("java.lang.String", [
                process.cwd(),
                "wl",
                "-h", APP_GLOBAL.GLOBAL_CONF.host,
                "-P", APP_GLOBAL.GLOBAL_CONF.port,
                "-u", APP_GLOBAL.GLOBAL_CONF.user,
                "-p", APP_GLOBAL.GLOBAL_CONF.password
            ]);

            Dplm.run(args,callbacks);

        },

        getStatusForFile: function (file, callbacks) {

            var args = java.newArray("java.lang.String", [
                process.cwd(),
                "st",
                "-h", APP_GLOBAL.GLOBAL_CONF.host,
                "-P", APP_GLOBAL.GLOBAL_CONF.port,
                "-u", APP_GLOBAL.GLOBAL_CONF.user,
                "-p", APP_GLOBAL.GLOBAL_CONF.password,
                file
            ]);

            Dplm.run(args,callbacks);

        },

        getStatusForPart: function (part, callbacks) {

            var args = java.newArray("java.lang.String", [
                process.cwd(),
                "st",
                "-h", APP_GLOBAL.GLOBAL_CONF.host,
                "-P", APP_GLOBAL.GLOBAL_CONF.port,
                "-u", APP_GLOBAL.GLOBAL_CONF.user,
                "-p", APP_GLOBAL.GLOBAL_CONF.password,
                "-w", part.getWorkspace(),
                "-o", part.getNumber(),
                "-r", part.getVersion()
            ]);

            Dplm.run(args,callbacks);

        },

        checkout: function (part, options, callbacks) {

            var _args = [
                process.cwd(),
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
                _args.push("-R");
            }
            if (options.force) {
                _args.push("f");
            }
            if (options.baseline) {
                _args.push("-b");
                _args.push(options.baseline);
            }

            var args = java.newArray("java.lang.String", _args);

            Dplm.run(args,callbacks);

        },

        checkin: function (part, callbacks) {

            var args = java.newArray("java.lang.String", [
                process.cwd(),
                "ci",
                "-h", APP_GLOBAL.GLOBAL_CONF.host,
                "-P", APP_GLOBAL.GLOBAL_CONF.port,
                "-u", APP_GLOBAL.GLOBAL_CONF.user,
                "-p", APP_GLOBAL.GLOBAL_CONF.password,
                "-w", part.getWorkspace(),
                "-o", part.getNumber(),
                "-r", part.getVersion()
            ]);

            Dplm.run(args,callbacks);

        },

        undoCheckout: function (part, callbacks) {

            var args = java.newArray("java.lang.String", [
                process.cwd(),
                "uco",
                "-h", APP_GLOBAL.GLOBAL_CONF.host,
                "-P", APP_GLOBAL.GLOBAL_CONF.port,
                "-u", APP_GLOBAL.GLOBAL_CONF.user,
                "-p", APP_GLOBAL.GLOBAL_CONF.password,
                "-w", part.getWorkspace(),
                "-o", part.getNumber(),
                "-r", part.getVersion()
            ]);

            Dplm.run(args,callbacks);

        },

        download: function (part, options, callbacks) {

            var _args = [
                process.cwd(),
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
                _args.push("-R");
            }
            if (options.force) {
                _args.push("f");
            }
            if (options.baseline) {
                _args.push("-b");
                _args.push(options.baseline);
            }

            var args = java.newArray("java.lang.String", _args);

            Dplm.run(args,callbacks);

        },

        createPart: function (part, filePath, callbacks) {

            var _args = [
                process.cwd(),
                "cr",
                "-h", APP_GLOBAL.GLOBAL_CONF.host,
                "-P", APP_GLOBAL.GLOBAL_CONF.port,
                "-u", APP_GLOBAL.GLOBAL_CONF.user,
                "-p", APP_GLOBAL.GLOBAL_CONF.password,
                "-w", part.getWorkspace(),
                "-o", part.getNumber()
            ];

            if (part.getName()) {
                _args.push("-N");
                _args.push(part.getName());
            }
            if (part.getDescription()) {
                _args.push("-d");
                _args.push(part.getDescription());
            }

            _args.push(filePath);

            var args = java.newArray("java.lang.String", _args);

            Dplm.run(args,callbacks);

        },

        getPartMastersCount: function (workspace, callbacks) {

            var args = java.newArray("java.lang.String", [
                process.cwd(),
                "pl",
                "-h", APP_GLOBAL.GLOBAL_CONF.host,
                "-P", APP_GLOBAL.GLOBAL_CONF.port,
                "-u", APP_GLOBAL.GLOBAL_CONF.user,
                "-p", APP_GLOBAL.GLOBAL_CONF.password,
                "-w", workspace,
                "-c"
            ]);

            Dplm.run(args,callbacks);

        },

        getPartMasters: function (workspace, start, max, callbacks) {

            var args = java.newArray("java.lang.String", [
                process.cwd(),
                "pl",
                "-h", APP_GLOBAL.GLOBAL_CONF.host,
                "-P", APP_GLOBAL.GLOBAL_CONF.port,
                "-u", APP_GLOBAL.GLOBAL_CONF.user,
                "-p", APP_GLOBAL.GLOBAL_CONF.password,
                "-s", Number(start).toString(),
                "-m", Number(max).toString(),
                "-w", workspace
            ]);

            Dplm.run(args,callbacks);

        },

        searchPartMasters: function (workspace, search, callbacks) {

            var args = java.newArray("java.lang.String", [
                process.cwd(),
                "s",
                "-h", APP_GLOBAL.GLOBAL_CONF.host,
                "-P", APP_GLOBAL.GLOBAL_CONF.port,
                "-u", APP_GLOBAL.GLOBAL_CONF.user,
                "-p", APP_GLOBAL.GLOBAL_CONF.password,
                "-w", workspace,
                "-s", search
            ]);

            Dplm.run(args,callbacks);
        },

        getBaselines: function (part, callbacks) {

            var args = java.newArray("java.lang.String", [
                process.cwd(),
                "bl",
                "-h", APP_GLOBAL.GLOBAL_CONF.host,
                "-P", APP_GLOBAL.GLOBAL_CONF.port,
                "-u", APP_GLOBAL.GLOBAL_CONF.user,
                "-p", APP_GLOBAL.GLOBAL_CONF.password,
                "-w", part.getWorkspace(),
                "-o", part.getNumber(),
                "-r", part.getVersion()
            ]);

            Dplm.run(args,callbacks);

        }
    };

    return Dplm;
});