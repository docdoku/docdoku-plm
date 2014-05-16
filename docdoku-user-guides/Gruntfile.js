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
            less: {
                files: ['<%= yeoman.app %>/assets/styles/{,*/}*.less'],
                tasks: ['less']
            },
            livereload: {
                files: [
                    '<%= yeoman.app %>/*.md',
                    '<%= yeoman.app %>/_i18n/{en,fr}/*.md',
                    '<%= yeoman.app %>/{,*/}*.html',
                    '{.tmp,<%= yeoman.app %>}/assets/styles/{,*/}*.css',
                    '{.tmp,<%= yeoman.app %>}/assets/scripts/{,*/}*.js',
                    '<%= yeoman.app %>/assets/images/{,*/}*.{png,jpg,jpeg,gif,webp,svg}'
                ],
                tasks: ['jekyll', 'livereload']
            }
        },
        connect: {
            options: {
                port: 9000,
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
        jshint: {
            options: {
                jshintrc: '.jshintrc'
            },
            all: [
                'Gruntfile.js',
                '<%= yeoman.app %>/assets/scripts/{,*/}*.js',
                '!<%= yeoman.app %>/assets/scripts/vendor/*'
            ]
        },
        mocha: {
            all: {
                options: {
                    run: true,
                    urls: ['http://localhost:<%= connect.options.port %>/index.html']
                }
            }
        },
        recess: {
            dist: {
                options: {
                    compile: true
                },
                files: {
                    '<%= yeoman.app %>/assets/styles/main.css': ['<%= yeoman.app %>/assets/styles/main.less']
                }
            }
        },
        less: {
          server: {           
            files: {
              '<%= yeoman.jkl %>/assets/styles/main.css': ['<%= yeoman.app %>/assets/styles/main.less']
            }
          },
          dist: {
            files: {
              '<%= yeoman.dist %>/assets/styles/main.css': ['<%= yeoman.app %>/assets/styles/main.less']
            }
          }
        },
        jekyll: {
            dist: {
                src: '<%= yeoman.app %>',
                dest: '<%= yeoman.jkl %>'
            }
        },
      
        copy: {
            dist: {
                files: [{
                    expand: true,
                    dot: true,
                    cwd: '<%= yeoman.jkl %>',
                    dest: '<%= yeoman.dist %>',
                    src: [
                        '*.{ico,txt}',
                        '.htaccess',
                        'assets/images/{,*/}*.{webp,gif}',
                        'assets/fonts/*'
                    ]
                }]
            },
            server: {
                files: [{
                    expand: true,
                    dot: true,
                    cwd: '<%= yeoman.app %>/bower_components/font-awesome/build/assets/font-awesome/font/',
                    dest: '<%= yeoman.app %>/assets/fonts/',
                    src: ['*']
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
    grunt.registerTask('server', function (target) {

        if (target === 'dist') {
            return grunt.task.run(['build', 'open', 'connect:dist:keepalive']);
        }

        grunt.task.run([
            'clean:server',
            'less',
            'copy:server',
            'jekyll',
            'copy:bower',
            'livereload-start',
            'connect:livereload',
            'open',
            'watch'
        ]);
    });


    // Build
    grunt.registerTask('build', [
        'clean:dist',
        'clean:server',
        'less',
        'copy:dist',        
        'jekyll:dist'
    ]);

    
    // Pdf
    grunt.registerTask('pdf', [
        'markdownpdf'
    ]);
};
