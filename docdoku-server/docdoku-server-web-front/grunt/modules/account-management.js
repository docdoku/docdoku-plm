var moduleName = 'accountManagement';

module.exports = {

    loadConf:function(config, grunt){

        config.copy.accountManagement=[];

        config.less.accountManagement = {
            options: {
                strictImports: false,
                paths: [
                  'app/less/c/'
                ]
            },
            files: {
                'app/account-management/main.css': 'app/less/account-management/style.less'
            }
        };

        config.clean.accountManagement = ['dist/account-management/*'];

        config.requirejs.accountManagement = {
            options: {
                name: '../../account-management/main',
                optimize: 'none',
                preserveLicenseComments: false,
                useStrict: true,
                wrap: true,
                inlineText: true,
                baseUrl: 'app/account-management/js',
                mainConfigFile: 'app/account-management/main.js',
                out: 'dist/account-management/main.js',
                paths: {localization: 'empty:'},
                findNestedDependencies: true
            }
        };

        config.uglify.accountManagement =  {
            files: {
                'dist/account-management/main.js': ['dist/account-management/main.js']
            }
        };

        config.cssmin.accountManagement = {
            files: {
                'dist/account-management/main.css': ['app/account-management/main.css']
            }
        };
        config.usemin.accountManagement = {
            html: ['dist/account-management/index.html'],
                css: ['dist/account-management/main.css'],
                options: {
                dirs: ['dist/account-management']
            }
        };
        config.htmlmin.accountManagement = {
            files: [{
                    expand: true,
                    cwd: 'app/account-management',
                    src: 'index.html',
                    dest: 'dist/account-management'
            }]
        };

    },

    loadTasks:function(grunt){

    }

};
