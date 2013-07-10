define(["storage"], function(Storage) {
    var cmd="";
    if(os.type() == "Windows_NT") {
        cmd='cd ' + Storage.getDirectory() + ' && ' + window.process.cwd() + '\\dplm\\dplm.bat';
    } else if (os.type() == "Linux" || "Darwin") {
        cmd = 'cd ' + Storage.getDirectory() + '; ' + 'sh '+ window.process.cwd() + '/dplm/dplm';
    } else {
    }

    var Commander = {

        chooseDirectory:function(callback) {
            var child = exec(cmd + " dc",
                function (error, stdout, stderr) {
                    if (error || stderr) {
                        console.log('exec error: ' + error);
                    } else {
                        Storage.setDirectory(stdout);
                    }
                    // Méthode de callback appelée par configuration_view.js
                    callback(stdout);
                });
        },

        getStatusForFile:function(file, callback) {
            var child = exec(cmd + " st" + this.getParams() + ' -j ' + file,
                function (error, stdout, stderr) {
                    if (error || stderr) {
                        console.log('exec error  : ' + file);
                    } else {
                        callback(stdout);
                    }
                });
        },

        getStatusForPartNumber:function(partNumber, version, callback) {
            var child = exec(cmd + " st" + this.getParams() + this.getParamsForPart(partNumber, version) + ' -j ',
                function (error, stdout, stderr) {
                    if (error || stderr) {
                        console.log('exec error  : ' + partNumber);
                    } else {
                        callback(stdout);
                    }
                });
        },

        checkout:function(partNumber, version, callback) {
            var child = exec(cmd + " co" + this.getParams() + this.getParamsForPart(partNumber, version),
            function (error, stdout, stderr) {
                if (error || stderr) {
                    console.log('exec error: ' + partNumber);
                }

                callback();
            });
        },

        checkin:function(partNumber, version, callback) {
            console.log(cmd + " ci" + this.getParams() + this.getParamsForPart(partNumber, version));
            var child = exec(cmd + " ci" + this.getParams() + this.getParamsForPart(partNumber, version),
                function (error, stdout, stderr) {
                    if (error || stderr) {
                        console.log('exec error: ' + partNumber);
                    }
                    callback();
                });
        },

        undoCheckout:function(partNumber, version, callback) {
            var child = exec(cmd + " uco" + this.getParams() + this.getParamsForPart(partNumber, version),
                function (error, stdout, stderr) {
                    if (error || stderr) {
                        console.log('exec error: ' + partNumber);
                    }
                    callback();
                });
        },

        get:function(file, callback) {
            var child = exec(cmd  + " get"+ this.getParams() + this.getParamsForPart(partNumber, version),
                function (error, stdout, stderr) {
                    if (error || stderr) {
                        console.log('exec error: ' + partNumber);
                    }
                    callback();
                });
        },

        createPart:function(part, filePath, callback, callbackError) {
            var child = exec(cmd + " cr" + this.getParams() + this.getCreatePartParams(part, filePath),
                function (error, stdout, stderr) {
                    if (error || stderr) {
                        console.log('exec error: ' + error);
                        console.log('exec stderr: ' + stderr);
                        callbackError();
                    } else {
                        callback();
                    }
                });
        },

        getPartMasters:function(callback) {
            var child = exec(cmd + " pl" + this.getParams(),
                function (error, stdout, stderr) {
                    if (error || stderr) {
                        console.log('exec error: ' + error);
                        console.log('exec stderr: ' + stderr);
                    } else {
                        callback(stdout);
                    }
                });
        },

        getParams:function() {
            var params = " -h " + Storage.getHost()
                + " -P " +  Storage.getPort()
                + " -w " + Storage.getWorkspace()
                + " -u " + Storage.getUser()
                + " -p " + Storage.getPwd()
                + " ";

            return params;
        },

        getParamsForPart: function(partNumber, version) {
            var params = "-o " + partNumber;

            params += " -r " + version;

            return params;
        },

        getCreatePartParams:function(part, filePath) {
            var params = "-o " + part.getNumber();

            if(part.getName()) {
                params += " -N " + part.getName();
            }

            if (part.getDescription()) {
                params += " -d " + part.getDescription();
            }

            params += " " + filePath;

            return params;
        }
     };

    return Commander;
});