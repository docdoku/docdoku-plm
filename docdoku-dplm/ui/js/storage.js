define(function () {

    /*
    * This class aims to store needed vars in local storage, such as global configuration, and user working
    * directories.
    *
    * */

    var HISTORY_SIZE = 5;

    var STORAGE_KEYS = {
        GLOBAL_CONF: "GLOBAL_CONF",
        LOCAL_PATHS: "LOCAL_PATHS",
        RECENTLY_USED : {
            LOCAL_PATHS:"R_LOCAL_PATHS",
            WORKSPACES:"R_WORKSPACES"
        }
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
            return !conf.host || !conf.user || !conf.password || !conf.port || !conf.javaHome;
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

        hasLocalPaths:function(){
            return  this.getLocalPaths().length > 0;
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

            var recentPaths = this.getRecentlyUsedPaths();
            while (recentPaths.indexOf(path) !== -1) {
                recentPaths.splice(recentPaths.indexOf(path), 1);
            }
            this.setRecentlyUsedPaths(recentPaths);
        },

        /*
        * Recently used paths
        * */

        addRecentlyUsedPath:function(path){
            var paths = this.getRecentlyUsedPaths();
            if(paths.indexOf(path) === -1){
                if(paths.length >= HISTORY_SIZE){
                    paths.shift();
                }
                paths.push(path);
                this.setRecentlyUsedPaths(paths);
                return true;
            }
            return false;
        },

        getRecentlyUsedPaths:function(){
            return JSON.parse(localStorage.getItem(STORAGE_KEYS.RECENTLY_USED.LOCAL_PATHS));
        },

        setRecentlyUsedPaths:function(paths){
            localStorage.setItem(STORAGE_KEYS.RECENTLY_USED.LOCAL_PATHS, JSON.stringify(paths));
        },

        /*
         * Recently used workspaces
         * */

        addRecentlyUsedWorkspace:function(workspace){
            var workspaces = this.getRecentlyUsedWorkspaces();
            if(workspaces.indexOf(workspace) === -1){
                if(workspaces.length >= HISTORY_SIZE){
                    workspaces.shift();
                }
                workspaces.push(workspace);
                this.setRecentlyUsedWorkspaces(workspaces);
                return true;
            }
            return false;
        },

        getRecentlyUsedWorkspaces:function(){
            return JSON.parse(localStorage.getItem(STORAGE_KEYS.RECENTLY_USED.WORKSPACES));
        },

        setRecentlyUsedWorkspaces:function(workspaces){
            localStorage.setItem(STORAGE_KEYS.RECENTLY_USED.WORKSPACES, JSON.stringify(workspaces));
        },

        /*
        * INIT
        * */

        init:function(){
            if(this.getLocalPaths() == null){
                this.setLocalPaths([]);
            }
            if(this.getGlobalConf() == null){
                this.setGlobalConf({});
            }
            if(this.getRecentlyUsedPaths() == null){
                this.setRecentlyUsedPaths([]);
            }
            if(this.getRecentlyUsedWorkspaces() == null){
                this.setRecentlyUsedWorkspaces([]);
            }
        }

    };

    // Init for first shot
    Storage.init();

    return Storage;
});