var moduleName = 'deploy';

module.exports = {

    name:moduleName,

    loadConf:function(config,grunt){
        config.copy.webapp = {
            files: [
                {
                    expand: true,
                    dot: false,
                    cwd: 'dist',
                    dest: '../docdoku-server-web/target/docdoku-server-web',
                    src: [
                        '**'
                    ]
                }
            ]
        };
    },

    loadTasks:function(grunt){
        grunt.registerTask('deploy',['build','copy:webapp','clean:dist']);
    }

};
