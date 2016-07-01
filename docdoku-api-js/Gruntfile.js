'use strict';

module.exports = function (grunt) {

    require('load-grunt-tasks')(grunt);

    grunt.initConfig({
        copy:{
            spec:{
                files : {
                    'lib/spec.json':'../docdoku-api/target/swagger/swagger.json'
                }
            },
            browser:{
                files : {
                    'dist/index.html':'example/browser.html'
                }
            }
        },
        clean: {
            options: {
                force: true
            },
            files:['lib/spec.json', 'dist']
        },

        browserify: {
            options:{
                standalone:true
            },
            dist: {
                files: {
                    'dist/docdoku-api.js': ['lib/*.js']
                }
            }
        }

    });

    grunt.registerTask('build',['clean', 'copy:spec','browserify:dist', 'copy:browser']);
};
