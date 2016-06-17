var moduleName = 'productManagement';

module.exports = {

    loadConf:function(config, grunt){

        config.copy.productManagement=[];

        config.less.productManagement = {
            options: {
                strictImports: false,
                paths: [
                  'app/less/c/'
                ]
            },
            files: {
                'app/product-management/main.css': 'app/less/product-management/style.less'
            }
        };

        config.clean.productManagement = ['dist/product-management/*'];

        config.requirejs.productManagement = {
            options: {
                name: '../../product-management/main',
                    optimize: 'none',
                    preserveLicenseComments: false,
                    useStrict: true,
                    wrap: true,
                    inlineText: true,
                    baseUrl: 'app/product-management/js',
                    mainConfigFile: 'app/product-management/main.js',
                    out: 'dist/product-management/main.js',
                    paths: {localization: 'empty:'},
                findNestedDependencies: true
            }
        };

        config.uglify.productManagement =  {
            files: {
                'dist/product-management/main.js': ['dist/product-management/main.js']
            }
        };

        config.cssmin.productManagement = {
            files: {
                'dist/product-management/main.css': ['app/product-management/main.css']
            }
        };
        config.usemin.productManagement = {
            html: ['dist/product-management/index.html'],
                css: ['dist/product-management/main.css'],
                options: {
                dirs: ['dist/product-management']
            }
        };
        config.htmlmin.productManagement = {
            files: [{
                    expand: true,
                    cwd: 'app/product-management',
                    src: 'index.html',
                    dest: 'dist/product-management'
            }]
        };

    },

    loadTasks:function(grunt){

    }

};
