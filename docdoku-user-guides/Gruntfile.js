/*
    Docdoku user guides grunt file

    Usage

        grunt serve        // launch a local web server on port 9000 with live reload
        grunt serve:dist   // launch a local web server on port 9000 with the build output
        grunt build        // launch a build
        grunt pdf          // export as pdf

*/

'use strict';

var lrSnippet = require('grunt-contrib-livereload/lib/utils').livereloadSnippet;

var mountFolder = function (connect, dir) {
    return connect.static(require('path').resolve(dir));
};

module.exports = function (grunt) {

    // load all grunt tasks
    require('matchdep').filterDev('grunt-*').forEach(grunt.loadNpmTasks);

    // configurable paths
    var yeomanConfig = {
        app: 'app',   // source code
        dist: 'dist',  // build dir
        jkl: 'jkl'    // jkl build dir
    };

    // plugins configuration
    grunt.initConfig({
        yeoman: yeomanConfig,
        watch: {           
            livereload: {
                files: [
                    '<%= yeoman.app %>/**/*.md',
                    '<%= yeoman.app %>/*.yml',
                    '<%= yeoman.app %>/{,*/}*.html',
                    '<%= yeoman.app %>/_plugins/*.rb',
                    '{.tmp,<%= yeoman.app %>}/assets/styles/{,*/}*.css',
                    '{.tmp,<%= yeoman.app %>}/assets/scripts/{,*/}*.js',
                    '<%= yeoman.app %>/assets/images/{,*/}*.{png,jpg,jpeg,gif,webp,svg}',
                    '<%= yeoman.app %>/assets/styles/{,*/}*.{scss,sass}'
                ],
                tasks: ['jekyll:jkl','less:server', 'copy:server','livereload']
            },
            less:{
                files:[
                    '<%= yeoman.app %>/assets/styles/*.less',    
                ],
                tasks:['less:server','livereload']
            }
        },
        connect: {
            options: {
                port: 9002,
                // change this to '0.0.0.0' to access the server from outside
                hostname: 'localhost'
            },
            livereload: {
                options: {
                    middleware: function (connect) {
                        return [
                            lrSnippet,
                            mountFolder(connect, '.tmp'),
                            mountFolder(connect, 'jkl')
                        ];
                    }
                }
            },           
            dist: {
                options: {
                    middleware: function (connect) {
                        return [
                            mountFolder(connect, 'dist')
                        ];
                    }
                }
            }
        },
        open: {
            server: {
                path: 'http://localhost:<%= connect.options.port %>'
            }
        },
        clean: {
            dist: {
                files: [{
                    dot: true,
                    src: [
                        '.tmp',
                        '<%= yeoman.dist %>/*',
                        '!<%= yeoman.dist %>/.git*'
                    ]
                }]
            },
            server: '.tmp'
        },
        less: {  
            server:{
                files: {
                  './jkl/assets/styles/main.css': ['<%= yeoman.app %>/assets/styles/main.less']
                }
            },
            dist:{
                files: {
                  './.tmp/styles/main.css': ['<%= yeoman.app %>/assets/styles/main.less']
                }
            }
        },
        jekyll: {
            jkl: {
                src: '<%= yeoman.app %>',
                dest: '<%= yeoman.jkl %>'
            },
            dist: {
                src: '<%= yeoman.app %>',
                dest: '<%= yeoman.dist %>'
            }
        },
        copy: {
            dist: {
                files: [{
                    expand: true,
                    dot: true,
                    cwd: '<%= yeoman.app%>',
                    dest: '<%= yeoman.dist %>',
                    src: [
                        '*.{ico,txt}',
                        '.htaccess',
                        'assets/images/{,*/}*.{webp,gif}',
                        'bower_components/bootstrap/fonts/*',
                        'bower_components/modernizr/modernizr.js',
                        'bower_components/bootstrap/js/*.js ',
                        'bower_components/jquery/jquery.js ',
                        'assets/fonts/*'
                    ]
                },{
                    expand: true,
                    dot: true,
                    cwd: '.tmp/styles/',
                    dest: '<%= yeoman.dist %>/assets/styles/',
                    src: ['*.css']
                }]
            },
            server: {
                files: [{
                    expand: true,
                    dot: true,
                    cwd: '<%= yeoman.app%>',
                    dest: '<%= yeoman.jkl %>',
                    src: [
                        '*.{ico,txt}',
                        '.htaccess',
                        'assets/images/{,*/}*.{webp,gif}',
                        'bower_components/bootstrap/fonts/*',
                        'bower_components/modernizr/modernizr.js',
                        'bower_components/bootstrap/js/*.js ',
                        'bower_components/jquery/jquery.js ',
                        'assets/fonts/*'
                    ]
                }]
            }
        },
       
        markdownpdf: {
            options: {},
            files: {
                src: "app/_i18n/en/Documentation_1.1.md",
                dest: ""
            }
        }
    });

    grunt.renameTask('regarde', 'watch');

    // Server
    grunt.registerTask('serve', function (target) {

        if (target === 'dist') {
            return grunt.task.run(['build', 'open', 'connect:dist:keepalive']);
        }

        grunt.task.run([
            'clean:server',
            'jekyll:jkl',
            'less',
            'copy:server',
            'livereload-start',
            'connect:livereload',
            'open:server',
            'watch'
        ]);
    });

    grunt.registerTask('build', [
        'clean:dist',
        'jekyll:dist',
        'less',
        'copy:dist'
    ]);

    
    // Pdf
    grunt.registerTask('pdf', [
        'markdownpdf'
    ]);
};
