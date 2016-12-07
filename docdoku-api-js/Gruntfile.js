'use strict';

module.exports = function (grunt) {

    require('load-grunt-tasks')(grunt);

    grunt.initConfig({

        clean: {
            options: {
                force: true
            },
            files: ['target/docdoku-plm-api/browser']
        },
        browserify: {
            options: {
                standalone: true
            },
            dist: {
                files: {
                    'target/docdoku-plm-api/browser/docdoku-plm-api.js': ['target/docdoku-plm-api/npm/src/index.js', 'exports/exports.js']
                }
            }
        },
        md2html: {
            doc: {
                options: {},
                files: [{
                    expand: true,
                    cwd: 'target/docdoku-plm-api/npm/docs/',
                    src: ['*.md'],
                    dest: 'target/docdoku-plm-api/site/docs/',
                    ext: '.html'
                },{
                    src: ['target/docdoku-plm-api/npm/README.md'],
                    dest: 'target/docdoku-plm-api/site/index.html'
                }]
            }
        },
        replace:{
            mdLinks:{
                src: ['target/docdoku-plm-api/site/**/*.html'],
                overwrite: true,
                replacements: [{
                    from: /\.md/g,
                    to: '.html'
                }]
            }
        }

    });

    grunt.registerTask('build', ['clean', 'browserify:dist', 'md2html:doc', 'replace:mdLinks']);
};


