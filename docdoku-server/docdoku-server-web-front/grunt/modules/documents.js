var moduleName = 'documents';

module.exports = {

    loadConf:function(config, grunt){

        config.copy.documents=[];

        config.less.documents = {
            options: {
                strictImports: false,
                paths: [
                  'app/less/documents/'
                ]
            },
            files: {
                'app/documents/main.css': 'app/less/documents/style.less'
            }
        };

        config.clean.documents = ['dist/documents/*'];

        config.requirejs.documents = {
            options: {
                name: '../../documents/main',
                optimize: 'none',
                preserveLicenseComments: false,
                useStrict: true,
                wrap: true,
                inlineText: true,
                baseUrl: 'app/documents/js',
                mainConfigFile: 'app/documents/main.js',
                out: 'dist/documents/main.js',
                paths: {localization: 'empty:'},
                findNestedDependencies: true
            }
        };

        config.uglify.documents =  {
            files: {
                'dist/documents/main.js': ['dist/documents/main.js']
            }
        };

        config.cssmin.documents = {
            files: {
                'dist/documents/main.css': ['app/documents/main.css']
            }
        };
        config.usemin.documents = {
            html: ['dist/documents/index.html'],
                css: ['dist/documents/main.css'],
                options: {
                dirs: ['dist/documents']
            }
        };
        config.htmlmin.documents = {
            files: [{
                    expand: true,
                    cwd: 'app/documents',
                    src: 'index.html',
                    dest: 'dist/documents'
            }]
        };

    },

    loadTasks:function(grunt){

    }

};
