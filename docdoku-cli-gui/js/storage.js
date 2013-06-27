define([""], function(){

    var STORAGE_KEYS = {
        HOST:      "host",
        PORT:      "port",
        USER:      "user",
        PWD:       "pwd",
        WORKSPACE: "workspace",
        DIRECTORY: "directory"
    };

    var Storage = {

        getHost:function() {
            return localStorage.getItem(STORAGE_KEYS.HOST);
        },

        getPort:function() {
            return localStorage.getItem(STORAGE_KEYS.PORT);
        },

        getUser:function() {
            return localStorage.getItem(STORAGE_KEYS.USER);
        },

        getPwd:function() {
            return localStorage.getItem(STORAGE_KEYS.PWD);
        },

        getWorkspace:function() {
            return localStorage.getItem(STORAGE_KEYS.WORKSPACE);
        },

        getDirectory:function() {
            return localStorage.getItem(STORAGE_KEYS.DIRECTORY);
        },

        setDirectory:function(value) {
            localStorage.setItem(STORAGE_KEYS.DIRECTORY, value);
        },

        setConfig:function(host, port, user, pwd, workspace, workingDir) {
            localStorage.setItem(STORAGE_KEYS.HOST, host);
            localStorage.setItem(STORAGE_KEYS.PORT, port);
            localStorage.setItem(STORAGE_KEYS.USER, user);
            localStorage.setItem(STORAGE_KEYS.PWD, pwd);
            localStorage.setItem(STORAGE_KEYS.WORKSPACE, workspace);
            localStorage.setItem(STORAGE_KEYS.DIRECTORY, workingDir);
        },

        isCompleted:function() {
            for (var key in STORAGE_KEYS) {
                if (localStorage.getItem(STORAGE_KEYS[key]) == null) {
                    return false;
                }
            }

            return true;
        }
    };

    return Storage;
});