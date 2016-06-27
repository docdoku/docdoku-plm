var moduleName = 'parts';

module.exports = {

    loadConf:function(config, grunt){

        config.copy.parts=[];

        config.less.parts = {
            options: {
                strictImports: false,
                paths: [
                  'app/less/parts/'
                ]
            },
            files: {
                'app/parts/main.css': 'app/less/parts/style.less'
            }
        };

        config.clean.parts = ['dist/parts/*'];

        config.requirejs.parts = {
            options: {
                name: '../../parts/main',
                optimize: 'none',
                preserveLicenseComments: false,
                useStrict: true,
                wrap: true,
                inlineText: true,
                baseUrl: 'app/parts/js',
                mainConfigFile: 'app/parts/main.js',
                out: 'dist/parts/main.js',
                paths: {localization: 'empty:'},
                findNestedDependencies: true
            }
        };

        config.uglify.parts =  {
            files: {
                'dist/parts/main.js': ['dist/parts/main.js']
            }
        };

        config.cssmin.parts = {
            files: {
                'dist/parts/main.css': ['app/parts/main.css']
            }
        };
        config.usemin.parts = {
            html: ['dist/parts/index.html'],
                css: ['dist/parts/main.css'],
                options: {
                dirs: ['dist/parts']
            }
        };
        config.htmlmin.parts = {
            files: [{
                    expand: true,
                    cwd: 'app/parts',
                    src: 'index.html',
                    dest: 'dist/parts'
            }]
        };

    },

    loadTasks:function(grunt){

    }

};
