var moduleName = 'download';

module.exports = {

    loadConf:function(config, grunt){

        config.less.download = {
            options: {
                strictImports: false,
                paths: [
                  'app/less/c/'
                ]
            },
            files: {
                'app/download/main.css': 'app/less/download/style.less'
            }
        };

        config.clean.download = ['dist/download/*'];

        config.copy.download = {
            files: [{
                expand: true,
                dot: false,
                cwd: 'app',
                dest: 'dist',
                src: [
                    'download/dplm/**'
                ]
            }]
        };

        config.requirejs.download = {
            options: {
                name: '../../download/main',
                optimize: 'none',
                preserveLicenseComments: false,
                useStrict: true,
                wrap: true,
                inlineText: true,
                baseUrl: 'app/download/js',
                mainConfigFile: 'app/download/main.js',
                out: 'dist/download/main.js',
                paths: {localization: 'empty:'},
                findNestedDependencies: true
            }
        };

        config.uglify.download =  {
            files: {
                'dist/download/main.js': ['dist/download/main.js']
            }
        };

        config.cssmin.download = {
            files: {
                'dist/download/main.css': ['app/download/main.css']
            }
        };
        config.usemin.download = {
            html: ['dist/download/index.html'],
                css: ['dist/download/main.css'],
                options: {
                dirs: ['dist/download']
            }
        };
        config.htmlmin.download = {
            files: [{
                    expand: true,
                    cwd: 'app/download',
                    src: 'index.html',
                    dest: 'dist/download'
            }]
        };

    },

    loadTasks:function(grunt){

    }

};
