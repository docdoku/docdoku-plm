module.exports = {

    loadConf:function(config,grunt){
    },

    loadTasks:function(grunt){

        grunt.registerTask('build-module', function (module) {
            return grunt.task.run([
                'clean:'+module,
                'requirejs:'+module,
                'uglify:'+module,
                'cssmin:'+module,
                'htmlmin:'+module,
                'usemin:'+module
            ]);
        });

        grunt.registerTask('build', [
            'clean:dist',
            'less',
            'copy:libs',
            'copy:assets',
            'copy:dmu',
            'copy:i18n',
            'build-module:main',
            'build-module:accountManagement',
            'build-module:workspaceManagement',
            'build-module:download',
            'build-module:documentManagement',
            'build-module:productManagement',
            'build-module:productStructure',
            'build-module:visualization',
            'build-module:changeManagement'
        ]);
    }
};
