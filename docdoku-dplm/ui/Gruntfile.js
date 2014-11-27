module.exports = function(grunt){

    'use strict';

    grunt.loadNpmTasks('grunt-contrib-jshint');
    grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-html2js');
    grunt.loadNpmTasks('grunt-ng-annotate');
    grunt.loadNpmTasks('grunt-usemin');
    grunt.loadNpmTasks('grunt-contrib-cssmin');
    grunt.loadNpmTasks('grunt-text-replace');

    grunt.initConfig ({

        jshint: {
            all: ['Gruntfile.js', 'app/js/**/*.js'],
            options: {
                ignores:[
                    'app/js/components/3d/utils/*.js',
                    'app/js/components/3d/loaders/*.js',
                    'app/js/components/3d/controls/*.js'
                ],
                strict:false,
                globals: {
                    angular: true,
                    localStorage:true
                }
            }
        },

        clean:  {
            dist: ['tmp', 'dist']
        },

        usemin: {
            html: 'dist/index.html'
        },

        cssmin: {
            dist: {
                files: {
                    'dist/app.css': ['dist/app.css']
                }
            }
        },

        html2js: {
            dist: {
                src: [ 'app/js/**/*.html' ],
                dest: 'tmp/templates.js',
                options:{
                    base:'',
                    rename:function (moduleName) {
                        return  moduleName.replace('app/js', 'js');
                    }
                },
                module:'dplm.templates'
            }
        },

        copy:{
            dist: {
                files: [{
                        expand: true,
                        cwd: 'app/',
                        src: [
                            'index.html',
                            'package.json',
                            'node_modules/**',
                            'docdoku-cli-jar-with-dependencies.jar',
                            'img/**'
                        ],
                        dest: 'dist/'
                },{
                    expand: true,
                    flatten: true,
                    cwd: 'app/',
                    src: [
                        'bower_components/threejs/build/three.js',
                        'bower_components/angular-aria/angular-aria.js',
                        'bower_components/hammerjs/hammer.js',
                        'bower_components/angular-animate/angular-animate.js',
                        'bower_components/angular-material/angular-material.js',
                        'bower_components/angular-route/angular-route.js',
                        'bower_components/angular-translate/angular-translate.js',
                        'bower_components/angular-uuid4/angular-uuid4.js'
                    ],
                    dest: 'tmp/libs/'
                },{
                    expand: true,
                    flatten: true,
                    cwd: 'app/',
                    src: [
                        'bower_components/fontawesome/fonts/**',
                    ],
                    dest: 'dist/fonts'
                },{
                    expand: true,
                    flatten: true,
                    cwd: 'app/',
                    src: [
                        'css/fonts/**',
                    ],
                    dest: 'dist/fonts'
                }]
            }
        },

        concat: {
            js: {
                src: ['app/bower_components/angular/angular.js','tmp/libs/*.js', 'app/js/**/*.js', 'tmp/templates.js'],
                dest: 'dist/app.js'
            },
            css:{
                src:[
                    'app/css/*.css',
                    'app/bower_components/angular-material/themes/blue-theme.css',
                    'app/bower_components/fontawesome/css/font-awesome.css'
                ],
                dest: 'dist/app.css'
            }
        },

        uglify: {
            dist: {
                files: {
                    'dist/app.js': [ 'dist/app.js' ]
                },
                options: {
                    mangle: true
                }
            }
        },

        ngAnnotate:{
            dist:{
                files:{
                    'dist/app.js':['dist/app.js']
                }
            },
            options: {
                singleQuotes: true
            }
        },

        replace: {
            dist: {
                src: ['dist/app.css'],
                dest: 'dist/app.css',
                replacements: [{
                    from: '../fonts',
                    to: 'fonts'
                }]
            }
        }

    });

    grunt.registerTask('build',[
        'jshint',
        'clean:dist',
        'copy:dist',
        'html2js:dist',
        'concat:js',
        'concat:css',
        'ngAnnotate:dist',
        'uglify:dist',
        'replace:dist',
        'cssmin:dist',
        'usemin'
    ]);

};