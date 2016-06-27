var moduleName = 'index';

module.exports = {

    loadConf:function(config, grunt){

        config.copy.main=[];

        config.less.main = {
            options: {
                strictImports: false,
                paths: [
                  'app/less/c/'
                ]
            },
            files: {
                'app/main/main.css': 'app/less/main/style.less'
            }
        };

        config.clean.main = ['dist/index.html','dist/main.css'];

        config.requirejs.main = {
            options: {
                name: '../../main/main',
                optimize: 'none',
                preserveLicenseComments: false,
                useStrict: true,
                wrap: true,
                inlineText: true,
                baseUrl: 'app/main/js',
                mainConfigFile: 'app/main/main.js',
                out: 'dist/main/main.js',
                paths: {localization: 'empty:'},
                findNestedDependencies: true
            }
        };

        config.uglify.main =  {
            files: {
                'dist/main/main.js': ['dist/main/main.js']
            }
        };

        config.cssmin.main = {
            files: {
                'dist/main/main.css': ['app/main/main.css']
            }
        };
        config.usemin.main = {
            html: ['dist/index.html'],
                css: ['dist/main/main.css'],
                options: {
                dirs: ['dist']
            }
        };
        config.htmlmin.main = {
            files: [{
                    expand: true,
                    cwd: 'app',
                    src: 'index.html',
                    dest: 'dist'
            }]
        };

    },

    loadTasks:function(grunt){

    }

};
