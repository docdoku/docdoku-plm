var moduleName = 'deploy';

module.exports = {

    name:moduleName,

    loadConf:function(config,grunt){

        var webapp = '../docdoku-server-web/target/docdoku-server-web';

        // clean be deprecated when everything will be moved
        config.clean.webapp = [
            webapp+'/img',
            webapp+'/fonts',
            webapp+'/images',
            webapp+'/sounds',
            webapp+'/bower_components',
            webapp+'/js',
            webapp+'/workspace-management',
            webapp+'/account-management',
            webapp+'/document-management',
            webapp+'/documents',
            webapp+'/product-management',
            webapp+'/product-structure',
            webapp+'/visualization',
            webapp+'/parts',
            webapp+'/main',
            webapp+'/change-management'
        ];

        config.copy.webapp = {
            files: [
                {
                    expand: true,
                    dot: false,
                    cwd: 'dist',
                    dest: webapp,
                    src: [
                        '**'
                    ]
                }
            ]
        };
    },

    loadTasks:function(grunt){
        grunt.registerTask('deploy',['clean:webapp','build','copy:webapp','clean:dist']);
    }

};
