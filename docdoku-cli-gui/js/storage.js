define([], function () {

    /*
    * This class aims to store needed vars in local storage, such as global configuration, and user working
    * directories.
    *
    * */

    var STORAGE_KEYS = {
        GLOBAL_CONF: "GLOBAL_CONF",
        LOCAL_PATHS: "LOCAL_PATHS"
    };

    var Storage = {

        /*
         * Global conf is an object like
         *
         * {host:"hostname",user:"user",password:"password",port:"port"}
         *
         */
        setGlobalConf: function (conf) {
            localStorage.setItem(STORAGE_KEYS.GLOBAL_CONF, JSON.stringify(conf));
        },

        getGlobalConf: function () {
            return JSON.parse(localStorage.getItem(STORAGE_KEYS.GLOBAL_CONF));
        },

        needsGlobalConf:function(){
            var conf = this.getGlobalConf();
            return !conf.host || !conf.user || !conf.password || !conf.port;
        },

        /*
         * Local paths is an array like
         *
         * ["/path/to/folder1", "/path/to/folder2", ... ]
         *
         */

        setLocalPaths: function (paths) {
            localStorage.setItem(STORAGE_KEYS.LOCAL_PATHS, JSON.stringify(paths));
        },

        getLocalPaths: function () {
            return JSON.parse(localStorage.getItem(STORAGE_KEYS.LOCAL_PATHS));
        },

        /*
         * Helpers for local paths
         * */

        addLocalPath: function (path) {
            var paths = this.getLocalPaths();
            if(paths.indexOf(path) === -1){
                paths.push(path);
                this.setLocalPaths(paths);
                return true;
            }
            return false;
        },

        removePath: function (path) {
            var paths = this.getLocalPaths();
            while (paths.indexOf(path) !== -1) {
                paths.splice(paths.indexOf(path), 1);
            }
            this.setLocalPaths(paths);
        },

        init:function(){
            if(this.getLocalPaths() == null){
                this.setLocalPaths([]);
            }
            if(this.getGlobalConf() == null){
                this.setGlobalConf({});
            }
        }

    };

    // Init for first shot
    Storage.init();

    return Storage;
});