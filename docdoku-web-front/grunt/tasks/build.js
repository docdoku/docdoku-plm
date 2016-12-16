module.exports = {

    loadConf:function(config,grunt){
        config.clean.dist=['.tmp', 'dist'];

        config.compress = {
            dist: {
                options: {
                    archive: 'target/docdoku-web-front.zip'
                },
                files: [
                    {expand: true, cwd: 'dist/', src: ['**'], dest: ''}
                ]
            }
        };


    },

    loadTasks:function(grunt){

        grunt.registerTask('build-module', function (module) {
            return grunt.task.run([
                'clean:'+module,
                'requirejs:'+module,
                'uglify:'+module,
                'cssmin:'+module,
                'htmlmin:'+module,
                'usemin:'+module,
                'copy:'+module
            ]);
        });

        grunt.registerTask('build', [
            'clean:dist',
            'less',
            'copy:libs',
            'copy:assets',
            'copy:properties',
            'copy:dmu',
            'copy:i18n',
            'build-module:main',
            'build-module:accountManagement',
            'build-module:workspaceManagement',
            'build-module:organizationManagement',
            'build-module:download',
            'build-module:documents',
            'build-module:parts',
            'build-module:documentManagement',
            'build-module:productManagement',
            'build-module:productStructure',
            'build-module:visualization',
            'build-module:changeManagement',
            'compress:dist'
        ]);
    }
};
