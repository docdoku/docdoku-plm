var moduleName = 'productStructure';

module.exports = {

    loadConf:function(config, grunt){

        config.copy.productStructure=[];

        config.less.productStructure = {
            options: {
                strictImports: false,
                paths: [
                  'app/less/c/'
                ]
            },
            files: {
                'app/product-structure/main.css': 'app/less/product-structure/style.less'
            }
        };

        config.clean.productStructure = ['dist/product-structure/*'];

        config.requirejs.productStructure = {
            options: {
                name: '../../product-structure/main',
                    optimize: 'none',
                    preserveLicenseComments: false,
                    useStrict: true,
                    wrap: true,
                    inlineText: true,
                    baseUrl: 'app/product-structure/js',
                    mainConfigFile: 'app/product-structure/main.js',
                    out: 'dist/product-structure/main.js',
                    paths: {localization: 'empty:'},
                findNestedDependencies: true
            }
        };

        config.uglify.productStructure =  {
            files: {
                'dist/product-structure/main.js': ['dist/product-structure/main.js']
            }
        };

        config.cssmin.productStructure = {
            files: {
                'dist/product-structure/main.css': ['app/product-structure/main.css']
            }
        };
        config.usemin.productStructure = {
            html: ['dist/product-structure/index.html'],
                css: ['dist/product-structure/main.css'],
                options: {
                dirs: ['dist/product-structure']
            }
        };
        config.htmlmin.productStructure = {
            files: [{
                    expand: true,
                    cwd: 'app/product-structure',
                    src: 'index.html',
                    dest: 'dist/product-structure'
            }]
        };

    },

    loadTasks:function(grunt){

    }

};
