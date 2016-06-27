var moduleName = 'visualization';

module.exports = {

    loadConf:function(config, grunt){

        config.copy.visualization=[];

        config.less.visualization = {
            options: {
                strictImports: false,
                paths: [
                    'app/less/c/'
                ]
            },
            files: {
                'app/visualization/main.css': 'app/less/visualization/style.less'
            }
        };

        config.clean.visualization = ['dist/visualization/*'];

        config.requirejs.visualization = {
            options: {
                name: '../../visualization/main',
                optimize: 'none',
                preserveLicenseComments: false,
                useStrict: true,
                wrap: true,
                inlineText: true,
                baseUrl: 'app/product-structure/js',
                mainConfigFile: 'app/visualization/main.js',
                out: 'dist/visualization/main.js',
                paths: {localization: 'empty:'},
                findNestedDependencies: true
            }
        };

        config.uglify.visualization =  {
            files: {
                'dist/visualization/main.js': ['dist/visualization/main.js']
            }
        };

        config.cssmin.visualization = {
            files: {
                'dist/visualization/main.css': ['app/visualization/main.css']
            }
        };
        config.usemin.visualization = {
            html: ['dist/visualization/index.html'],
            css: ['dist/visualization/main.css'],
            options: {
                dirs: ['dist/visualization']
            }
        };
        config.htmlmin.visualization = {
            files: [{
                expand: true,
                cwd: 'app/visualization',
                src: 'index.html',
                dest: 'dist/visualization'
            }]
        };

    },

    loadTasks:function(grunt){

    }

};
