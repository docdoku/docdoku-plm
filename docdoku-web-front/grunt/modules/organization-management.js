var moduleName = 'organizationManagement';

module.exports = {

    loadConf: function (config, grunt) {

        config.copy.organizationManagement = [];

        config.less.organizationManagement = {
            options: {
                strictImports: false,
                paths: [
                    'app/less/organization-management/'
                ]
            },
            files: {
                'app/organization-management/main.css': 'app/less/organization-management/style.less'
            }
        };

        config.clean.organizationManagement = ['dist/organization-management/*'];

        config.requirejs.organizationManagement = {
            options: {
                name: '../../organization-management/main',
                optimize: 'none',
                preserveLicenseComments: false,
                useStrict: true,
                wrap: true,
                inlineText: true,
                baseUrl: 'app/organization-management/js',
                mainConfigFile: 'app/organization-management/main.js',
                out: 'dist/organization-management/main.js',
                paths: {localization: 'empty:'},
                findNestedDependencies: true
            }
        };

        config.uglify.organizationManagement = {
            files: {
                'dist/organization-management/main.js': ['dist/organization-management/main.js']
            }
        };

        config.cssmin.organizationManagement = {
            files: {
                'dist/organization-management/main.css': ['app/organization-management/main.css']
            }
        };
        config.usemin.organizationManagement = {
            html: ['dist/organization-management/index.html'],
            css: ['dist/organization-management/main.css'],
            options: {
                dirs: ['dist/organization-management']
            }
        };
        config.htmlmin.organizationManagement = {
            files: [{
                expand: true,
                cwd: 'app/organization-management',
                src: 'index.html',
                dest: 'dist/organization-management'
            }]
        };

    },

    loadTasks: function (grunt) {

    }

};
