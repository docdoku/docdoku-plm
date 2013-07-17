define([], function () {

    var escapeShell = function(cmd) {
        return '"'+cmd+'"';
    };

    var classPath = window.process.cwd() + '/dplm/docdoku-cli-jar-with-dependencies.jar' ;

    var WindowsCMD = {
        base:function(){
            return 'cd "' + APP_GLOBAL.CURRENT_PATH + '" && '
                + '"%JAVA_HOME%\\bin\\java" -Xmx1024M -cp "' + classPath + '" com.docdoku.cli.MainCommand ';
        },
        explore:function(path){
            return 'start "'+path+'"';
        }
    };

    var LinuxCMD = {
        base:function(){
            return 'cd "' + APP_GLOBAL.CURRENT_PATH + '"; '
                + '"$JAVA_HOME/bin/java" -Xmx1024M -cp "' + classPath + '" com.docdoku.cli.MainCommand ';
        },
        explore:function(path){
            return 'nautilus "' + path + '"';
        }
    };

    var OsxCMD = {
        base:function(){
            return 'cd "' + APP_GLOBAL.CURRENT_PATH + '"; '
                + '"$JAVA_HOME/bin/java" -Xmx1024M -cp "' + classPath + '" com.docdoku.cli.MainCommand ';
        },
        explore:function(path){
            return 'open "' + path + '"';
        }
    };

    var cmd = {};

    switch(os.type()){
        case "Windows_NT" : _.extend(cmd,WindowsCMD); break;
        case "Linux" : _.extend(cmd,LinuxCMD); break;
        case "Darwin" : _.extend(cmd,OsxCMD); break;
        default :_.extend(cmd,LinuxCMD); break;
    }

    _.extend(cmd,{

        getWorkspaces:function(){
            return this.base() + " wl"
                + " -h " + APP_GLOBAL.GLOBAL_CONF.host
                + " -P " + APP_GLOBAL.GLOBAL_CONF.port
                + " -u " + APP_GLOBAL.GLOBAL_CONF.user
                + " -p " + APP_GLOBAL.GLOBAL_CONF.password
                + " -j";
        },

        getStatusForFile:function(file){
            return this.base() + " st"
                + " -h " + APP_GLOBAL.GLOBAL_CONF.host
                + " -P " + APP_GLOBAL.GLOBAL_CONF.port
                + " -u " + APP_GLOBAL.GLOBAL_CONF.user
                + " -p " + APP_GLOBAL.GLOBAL_CONF.password
                + " -j "
                + escapeShell(file);
        },

        getStatusForPart: function (part){
            return this.base() + " st"
                + " -h " + APP_GLOBAL.GLOBAL_CONF.host
                + " -P " + APP_GLOBAL.GLOBAL_CONF.port
                + " -u " + APP_GLOBAL.GLOBAL_CONF.user
                + " -p " + APP_GLOBAL.GLOBAL_CONF.password
                + " -w " + escapeShell(part.getWorkspace())
                + " -o " + escapeShell(part.getNumber())
                + " -r " + escapeShell(part.getVersion())
                + " -j";
        },

        checkout:function(part){
            return this.base() + " co"
                + " -h " + APP_GLOBAL.GLOBAL_CONF.host
                + " -P " + APP_GLOBAL.GLOBAL_CONF.port
                + " -u " + APP_GLOBAL.GLOBAL_CONF.user
                + " -p " + APP_GLOBAL.GLOBAL_CONF.password
                + " -w " + escapeShell(part.getWorkspace())
                + " -o " + escapeShell(part.getNumber())
                + " -r " + escapeShell(part.getVersion());
        },

        checkin:function(part){
            return this.base() + " ci"
                + " -h " + APP_GLOBAL.GLOBAL_CONF.host
                + " -P " + APP_GLOBAL.GLOBAL_CONF.port
                + " -u " + APP_GLOBAL.GLOBAL_CONF.user
                + " -p " + APP_GLOBAL.GLOBAL_CONF.password
                + " -w " + escapeShell(part.getWorkspace())
                + " -o " + escapeShell(part.getNumber())
                + " -r " + escapeShell(part.getVersion());
        },

        undoCheckout:function(part){
            return this.base() + " uco"
                + " -h " + APP_GLOBAL.GLOBAL_CONF.host
                + " -P " + APP_GLOBAL.GLOBAL_CONF.port
                + " -u " + APP_GLOBAL.GLOBAL_CONF.user
                + " -p " + APP_GLOBAL.GLOBAL_CONF.password
                + " -w " + escapeShell(part.getWorkspace())
                + " -o " + escapeShell(part.getNumber())
                + " -r " + escapeShell(part.getVersion());
        },

        createPart:function(part, filePath){

            var optionalParams = function(){
                var opt = "";
                if (part.getName()) {
                    opt += " -N " + escapeShell(part.getName());
                }
                if (part.getDescription()) {
                    opt += " -d " + escapeShell(part.getDescription().replace(/"/g,"\\\""));
                }
                return opt;
            };

            return this.base() + " cr"
                + " -h " + APP_GLOBAL.GLOBAL_CONF.host
                + " -P " + APP_GLOBAL.GLOBAL_CONF.port
                + " -u " + APP_GLOBAL.GLOBAL_CONF.user
                + " -p " + APP_GLOBAL.GLOBAL_CONF.password
                + " -w " + escapeShell(part.getWorkspace())
                + " -o " + escapeShell(part.getNumber())
                + optionalParams(part, filePath)
                + " " + escapeShell(filePath)

        },

        getPartMasters:function(workspace){
            return this.base() + " pl"
                + " -h " + APP_GLOBAL.GLOBAL_CONF.host
                + " -P " + APP_GLOBAL.GLOBAL_CONF.port
                + " -u " + APP_GLOBAL.GLOBAL_CONF.user
                + " -p " + APP_GLOBAL.GLOBAL_CONF.password
                + " -w " + escapeShell(workspace);
        }

    });

    var Commander = {

        openPath:function(path){
            var c = cmd.explore(path);
            exec(c);
        },

        getWorkspaces: function (callbacks) {
            var c = cmd.getWorkspaces();
            console.log("Commander.getWorkspaces : " + c);
            exec(c, function (error, stdout, stderr) {
                if (error || stderr) {
                    console.log('getWorkspaces : exec error' + stderr + " " + error);
                    callbacks.error(stdout);
                } else {
                    callbacks.success(stdout);
                }
            });
        },

        getStatusForFile: function (file, callback) {
            var c = cmd.getStatusForFile(file);
            console.log("Commander.getStatusForFile : " + c);
            exec(c, function (error, stdout, stderr) {
                if (error || stderr) {
                    console.log('getStatusForFile : exec error' + stderr + " " + error);
                } else {
                    callback(stdout);
                }
            });
        },

        getStatusForPart: function (part, callback) {
            var c = cmd.getStatusForPart(part) ;
            console.log("Commander.getStatusForPart : " + c);
            exec(c,function (error, stdout, stderr) {
                if (error || stderr) {
                    console.log('getStatusForPart : exec error' + stderr + " " + error);
                } else {
                    callback(stdout);
                }
            });
        },

        checkout: function (part, callback) {
            var c = cmd.checkout(part);
            console.log("Commander.checkout : " + c);
            exec(c,function (error, stdout, stderr) {
                if (error || stderr) {
                    console.log('checkout : exec error' + stderr + " " + error);
                }

                callback();
            });
        },

        checkin: function (part, callback) {
            var c = cmd.checkin(part);
            console.log("Commander.checkin : " + c);
            exec(c, function (error, stdout, stderr) {
                if (error || stderr) {
                    console.log('checkin : exec error' + stderr + " " + error);
                }
                callback();
            });
        },

        undoCheckout: function (part, callback) {
            var c = cmd.undoCheckout(part);
            console.log("Commander.undoCheckout : " + c);
            exec(c, function (error, stdout, stderr) {
                if (error || stderr) {
                    console.log('undoCheckout : exec error' + stderr + " " + error);
                }
                callback();
            });
        },

        createPart: function (part, filePath, callback, callbackError) {
            var c = cmd.createPart(part, filePath);
            console.log("Commander.createPart : " + c);
            exec(c, function (error, stdout, stderr) {
                if (error || stderr) {
                    console.log('createPart : exec error' + stderr + " " + error);
                    callbackError();
                } else {
                    callback();
                }
            });
        },

        getPartMasters: function (workspace,callback) {
            var c = cmd.getPartMasters(workspace);
            console.log("Commander.getPartMasters : " + c);
            exec(c,function (error, stdout, stderr) {
                if (error || stderr) {
                    console.log('getPartMasters : exec error' + stderr + " " + error);
                } else {
                    callback(stdout);
                }
            });
        }
    };

    return Commander;
});