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
                options: {
                    layout: 'doc-assets/doc.layout.html'
                },
                files: [{
                    expand: true,
                    cwd: 'target/docdoku-plm-api/npm/docs/',
                    src: ['*.md'],
                    dest: 'target/docdoku-plm-api/site/docs/',
                    ext: '.html',
                    options: {
                        layout: 'doc-assets/doc.layout.html'
                    }
                }]
            },
            index: {
                options: {
                    layout: 'doc-assets/index.layout.html'
                },
                files: [{
                    src: ['target/docdoku-plm-api/npm/README.md'],
                    dest: 'target/docdoku-plm-api/site/index.html'
                }]
            }
        },
        replace: {
            mdLinks: {
                src: ['target/docdoku-plm-api/site/**/*.html'],
                overwrite: true,
                replacements: [{
                    from: /\.md/g,
                    to: '.html'
                }]
            }
        },
        copy:{
            css:{
                src: 'node_modules/github-markdown-css/github-markdown.css',
                dest: 'target/docdoku-plm-api/site/style.css'
            }
        }

    });

    grunt.registerTask('build', ['clean', 'browserify:dist', 'md2html:doc', 'md2html:index', 'copy:css','replace:mdLinks']);
};


