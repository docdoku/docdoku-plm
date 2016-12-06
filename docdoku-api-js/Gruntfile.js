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
        }

    });

    grunt.registerTask('build', ['clean', 'browserify:dist']);
};


