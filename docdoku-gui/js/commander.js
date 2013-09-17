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

        checkout:function(part, options){

            var optionalParams = function(){
                var opt = "";
                if (options.recursive) {
                    opt += " -R ";
                }
                if (options.force) {
                    opt += " -f ";
                }
                if(options.baseline){
                    opt += " -b " + escapeShell(options.baseline)
                }
                return opt;
            };

            return this.base() + " co"
                + " -h " + APP_GLOBAL.GLOBAL_CONF.host
                + " -P " + APP_GLOBAL.GLOBAL_CONF.port
                + " -u " + APP_GLOBAL.GLOBAL_CONF.user
                + " -p " + APP_GLOBAL.GLOBAL_CONF.password
                + " -w " + escapeShell(part.getWorkspace())
                + " -o " + escapeShell(part.getNumber())
                + " -r " + escapeShell(part.getVersion())
                + optionalParams();
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

        get:function(part,options){

            var optionalParams = function(){
                var opt = "";
                if (options.recursive) {
                    opt += " -R ";
                }
                if (options.force) {
                    opt += " -f ";
                }
                if(options.baseline){
                    opt += " -b " + escapeShell(options.baseline)
                }
                return opt;
            };

            return this.base() + " get"
                + " -h " + APP_GLOBAL.GLOBAL_CONF.host
                + " -P " + APP_GLOBAL.GLOBAL_CONF.port
                + " -u " + APP_GLOBAL.GLOBAL_CONF.user
                + " -p " + APP_GLOBAL.GLOBAL_CONF.password
                + " -R "
                + " -w " + escapeShell(part.getWorkspace())
                + " -o " + escapeShell(part.getNumber())
                + " -r " + escapeShell(part.getVersion())
                +optionalParams();
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
                + optionalParams()
                + " " + escapeShell(filePath)

        },

        getPartMasters:function(workspace, start, max){
            return this.base() + " pl"
                + " -h " + APP_GLOBAL.GLOBAL_CONF.host
                + " -P " + APP_GLOBAL.GLOBAL_CONF.port
                + " -u " + APP_GLOBAL.GLOBAL_CONF.user
                + " -p " + APP_GLOBAL.GLOBAL_CONF.password
                + " -s " + start
                + " -m " + max
                + " -w " + escapeShell(workspace);
        },

        searchPartMasters:function(workspace, search){
            return this.base() + " search"
                + " -h " + APP_GLOBAL.GLOBAL_CONF.host
                + " -P " + APP_GLOBAL.GLOBAL_CONF.port
                + " -u " + APP_GLOBAL.GLOBAL_CONF.user
                + " -p " + APP_GLOBAL.GLOBAL_CONF.password
                + " -s " + escapeShell(search)
                + " -w " + escapeShell(workspace);
        },


        getPartMastersCount:function(workspace){
            return this.base() + " pl"
                + " -h " + APP_GLOBAL.GLOBAL_CONF.host
                + " -P " + APP_GLOBAL.GLOBAL_CONF.port
                + " -u " + APP_GLOBAL.GLOBAL_CONF.user
                + " -p " + APP_GLOBAL.GLOBAL_CONF.password
                + " -c "
                + " -w " + escapeShell(workspace);
        },

        getBaselines:function(part){
            return this.base() + " bl"
                + " -h " + APP_GLOBAL.GLOBAL_CONF.host
                + " -P " + APP_GLOBAL.GLOBAL_CONF.port
                + " -u " + APP_GLOBAL.GLOBAL_CONF.user
                + " -p " + APP_GLOBAL.GLOBAL_CONF.password
                + " -w " + escapeShell(part.getWorkspace())
                + " -o " + escapeShell(part.getNumber())
                + " -r " + escapeShell(part.getVersion());
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

        checkout: function (part, options, callback) {
            var c = cmd.checkout(part,options);
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

        getPartMasters: function (workspace, start, max, callback) {
            var c = cmd.getPartMasters(workspace, start, max);
            console.log("Commander.getPartMasters : " + c);
            exec(c,{ maxBuffer: 1274916 * 2  },function (error, stdout, stderr) {
                if (error || stderr) {
                    console.log('getPartMasters : exec error' + stderr + " " + error);
                } else {
                    callback(stdout);
                }
            });
        },
        searchPartMasters: function (workspace, search, callback) {
                    var c = cmd.searchPartMasters(workspace, search);
                    console.log("Commander.searchPartMasters : " + c);
                    exec(c,{ maxBuffer: 1274916 * 2  },function (error, stdout, stderr) {
                        if (error || stderr) {
                            console.log('searchPartMasters : exec error' + stderr + " " + error);
                        } else {
                            callback(stdout);
                        }
                    });
                },

        getPartMastersCount: function (workspace, callback) {
            var c = cmd.getPartMastersCount(workspace);
            console.log("Commander.getPartMastersCount : " + c);
            exec(c,{ maxBuffer: 1274916 * 2  },function (error, stdout, stderr) {
                if (error || stderr) {
                    console.log('getPartMastersCount : exec error' + stderr + " " + error);
                } else {
                    callback(stdout);
                }
            });
        },

        get:function(part,options,callback){
            var c = cmd.get(part,options);
            console.log("Commander.get : " + c);
            exec(c, function (error, stdout, stderr) {
                if (error || stderr) {
                    console.log('get : exec error' + stderr + " " + error);
                }
                callback();
            });
        },
        getBaselines:function(part,callback){
            var c = cmd.getBaselines(part);
            console.log("Commander.getBaselines : " + c);
            exec(c, function (error, stdout, stderr) {
                console.log(stdout);
                if (error || stderr) {
                    console.log('getBaselines : exec error' + stderr + " " + error);
                }
                callback(stdout);
            });
        }
    };

    return Commander;
});