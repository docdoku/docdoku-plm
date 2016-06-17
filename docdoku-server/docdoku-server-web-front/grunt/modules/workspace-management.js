var moduleName = 'workspaceManagement';

module.exports = {

    loadConf:function(config, grunt){

        config.copy.workspaceManagement=[];

        config.less.workspaceManagement = {
            options: {
                strictImports: false,
                paths: [
                    'app/less/workspace-management/'
                ]
            },
            files: {
                'app/workspace-management/main.css': 'app/less/workspace-management/style.less'
            }
        };

        config.clean.workspaceManagement = ['dist/workspace-management/*'];

        config.requirejs.workspaceManagement = {
            options: {
                name: '../../workspace-management/main',
                optimize: 'none',
                preserveLicenseComments: false,
                useStrict: true,
                wrap: true,
                inlineText: true,
                baseUrl: 'app/workspace-management/js',
                mainConfigFile: 'app/workspace-management/main.js',
                out: 'dist/workspace-management/main.js',
                paths: {localization: 'empty:'},
                findNestedDependencies: true
            }
        };

        config.uglify.workspaceManagement =  {
            files: {
                'dist/workspace-management/main.js': ['dist/workspace-management/main.js']
            }
        };

        config.cssmin.workspaceManagement = {
            files: {
                'dist/workspace-management/main.css': ['app/workspace-management/main.css']
            }
        };
        config.usemin.workspaceManagement = {
            html: ['dist/workspace-management/index.html'],
            css: ['dist/workspace-management/main.css'],
            options: {
                dirs: ['dist/workspace-management']
            }
        };
        config.htmlmin.workspaceManagement = {
            files: [{
                expand: true,
                cwd: 'app/workspace-management',
                src: 'index.html',
                dest: 'dist/workspace-management'
            }]
        };

    },

    loadTasks:function(grunt){

    }

};
