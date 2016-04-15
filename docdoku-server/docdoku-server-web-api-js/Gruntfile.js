'use strict';

module.exports = function (grunt) {

    require('load-grunt-tasks')(grunt);

    grunt.initConfig({
        concat: {
            options: {
                separator: ';'
            },
            dist: {
                src: [
                    'bower_components/swagger-js/browser/swagger-client.js',
                    'js/docdokuplm-client.js'
                ],
                dest: 'dist/docdoku-plm-api.js'
            }
        }
        ,
        uglify:{
            options: {
                mangle: false
            },
            my_target: {
                files: {
                    'dist/docdoku-plm-api.min.js': ['dist/docdoku-plm-api.js']
                }
            }
        },
        clean: {
            options: {
                force: true
            },
            dist: ['dist/*']
        },
        copy:{
            swaggerUi : {
                files: [{
                    expand: true,
                    cwd: 'bower_components/swagger-ui/dist/',
                    src: ['**'],
                    dest: 'dist/swagger-ui/'
                }]
            },
            example : {
                files: [{
                    expand: true,
                    cwd: 'example/',
                    src: ['**'],
                    dest: 'dist/example/'
                }]
            }
        }
    });

    grunt.registerTask('build',['copy:swaggerUi','concat','uglify']);
};
