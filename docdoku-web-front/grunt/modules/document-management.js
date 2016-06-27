var moduleName = 'documentManagement';

module.exports = {

    loadConf:function(config, grunt){

        config.copy.documentManagement=[];

        config.less.documentManagement = {
            options: {
                strictImports: false,
                paths: [
                  'app/less/document-management/'
                ]
            },
            files: {
                'app/document-management/main.css': 'app/less/document-management/style.less'
            }
        };

        config.clean.documentManagement = ['dist/document-management/*'];

        config.requirejs.documentManagement = {
            options: {
                name: '../../document-management/main',
                optimize: 'none',
                preserveLicenseComments: false,
                useStrict: true,
                wrap: true,
                inlineText: true,
                baseUrl: 'app/document-management/js',
                mainConfigFile: 'app/document-management/main.js',
                out: 'dist/document-management/main.js',
                paths: {localization: 'empty:'},
                findNestedDependencies: true
            }
        };

        config.uglify.documentManagement =  {
            files: {
                'dist/document-management/main.js': ['dist/document-management/main.js']
            }
        };

        config.cssmin.documentManagement = {
            files: {
                'dist/document-management/main.css': ['app/document-management/main.css']
            }
        };
        config.usemin.documentManagement = {
            html: ['dist/document-management/index.html'],
                css: ['dist/document-management/main.css'],
                options: {
                dirs: ['dist/document-management']
            }
        };
        config.htmlmin.documentManagement = {
            files: [{
                    expand: true,
                    cwd: 'app/document-management',
                    src: 'index.html',
                    dest: 'dist/document-management'
            }]
        };

    },

    loadTasks:function(grunt){

    }

};
