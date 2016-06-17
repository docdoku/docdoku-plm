var moduleName = 'changeManagement';

module.exports = {

    loadConf:function(config, grunt){

        config.copy.changeManagement=[];

        config.less.changeManagement = {
            options: {
                strictImports: false,
                paths: [
                  'app/less/c/'
                ]
            },
            files: {
                'app/change-management/main.css': 'app/less/change-management/style.less'
            }
        };

        config.clean.changeManagement = ['dist/change-management/*'];

        config.requirejs.changeManagement = {
            options: {
                name: '../../change-management/main',
                    optimize: 'none',
                    preserveLicenseComments: false,
                    useStrict: true,
                    wrap: true,
                    inlineText: true,
                    baseUrl: 'app/change-management/js',
                    mainConfigFile: 'app/change-management/main.js',
                    out: 'dist/change-management/main.js',
                    paths: {localization: 'empty:'},
                findNestedDependencies: true
            }
        };

        config.uglify.changeManagement =  {
            files: {
                'dist/change-management/main.js': ['dist/change-management/main.js']
            }
        };

        config.cssmin.changeManagement = {
            files: {
                'dist/change-management/main.css': ['app/change-management/main.css']
            }
        };
        config.usemin.changeManagement = {
            html: ['dist/change-management/index.html'],
                css: ['dist/change-management/main.css'],
                options: {
                dirs: ['dist/change-management']
            }
        };
        config.htmlmin.changeManagement = {
            files: [{
                    expand: true,
                    cwd: 'app/change-management',
                    src: 'index.html',
                    dest: 'dist/change-management'
            }]
        };

    },

    loadTasks:function(grunt){

    }

};
