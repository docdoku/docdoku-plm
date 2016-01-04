'use strict';
var LIVERELOAD_PORT = 35730;
var SERVER_HOSTNAME = 'localhost';
var SERVER_PORT = 9001;
var DEV_HOSTNAME = 'localhost';
var DEV_PORT = 8989;
var lrSnippet = require('connect-livereload')({port: LIVERELOAD_PORT});

var mountFolder = function (connect, dir) {
    return connect.static(require('path').resolve(dir));
};

module.exports = function (grunt) {

    require('time-grunt')(grunt);
    require('load-grunt-tasks')(grunt);

    var yeoman = {
        app: 'app',
        dist: 'dist',
	    webapp: '../target/docdoku-server-web',
        tests: 'tests'
    };

    grunt.initConfig({
        yeoman: yeoman,
        watch: {
            dev: {
                options: {
                    nospawn: true,
                    livereload: grunt.option('livereloadport') || LIVERELOAD_PORT
                },
                files: [
                    '<%= yeoman.app %>/document-management/*.html',
                    '<%= yeoman.app %>/product-management/*.html',
                    '<%= yeoman.app %>/product-structure/*.html',
                    '<%= yeoman.app %>/visualization/*.html',
                    '<%= yeoman.app %>/change-management/*.html',
                    '<%= yeoman.app %>/images/**',
                    '{.tmp,<%= yeoman.app %>}/js/**',
                    '<%= yeoman.app %>/less/**'
                ]
            },
            tests:{
                files:['<%= yeoman.tests %>/js/**/*.js'],
                tasks:['execute:tests','watch:tests']
            }
        },
        connect: {
            options: {
                port: grunt.option('port') || SERVER_PORT,
                hostname: SERVER_HOSTNAME
            },

            livereload: {
                options: {
                    middleware: function (connect) {
                        return [
                            lrSnippet,
                            mountFolder(connect, '.tmp'),
                            mountFolder(connect, yeoman.app)
                        ];
                    }
                }
            },
            dist: {
                options: {
                    middleware: function (connect) {
                        return [
                            mountFolder(connect, yeoman.dist)
                        ];
                    }
                }
            },
	        app: {
                options: {
                    middleware: function (connect) {
                        return [
                            mountFolder(connect, yeoman.app)
                        ];
                    }
                }
            }
        },
        open: {
            server: {
                path: 'http://localhost:<%= connect.options.port %>'
            },
	        dev:{
		        path: 'http://'+DEV_HOSTNAME+':'+DEV_PORT
	        }
        },
        clean: {
            options: {
                force: true
            },
            documentManagement: ['<%= yeoman.dist %>/document-management/*'],
            productManagement: ['<%= yeoman.dist %>/product-management/*'],
            productStructure: ['<%= yeoman.dist %>/product-structure/*'],
            productFrame: ['<%= yeoman.dist %>/visualization/*'],
            changeManagement: ['<%= yeoman.dist %>/change-management/*'],
            dist: ['.tmp', '<%= yeoman.dist %>/*'],
            server: '.tmp',
            webapp:[
                '<%= yeoman.webapp %>/img',
                '<%= yeoman.webapp %>/fonts',
                '<%= yeoman.webapp %>/images',
                '<%= yeoman.webapp %>/sounds',
                '<%= yeoman.webapp %>/js/product-structure',
                '<%= yeoman.webapp %>/js/home',
                '<%= yeoman.webapp %>/bower_components',
                '<%= yeoman.webapp %>/document-management',
                '<%= yeoman.webapp %>/product-management',
                '<%= yeoman.webapp %>/visualization',
                '<%= yeoman.webapp %>/change-management',
                '<%= yeoman.webapp %>/product-structure/index.html',
                '<%= yeoman.webapp %>/product-structure/main.js',
                '<%= yeoman.webapp %>/product-structure/main.css',
                '<%= yeoman.webapp %>/js/localization'
            ]
        },
        requirejs: {

            documentManagement: {
                options: {
                    name: '../../document-management/main',
                    optimize: 'none',
                    preserveLicenseComments: false,
                    useStrict: true,
                    wrap: true,
                    inlineText: true,
                    baseUrl: '<%= yeoman.app %>/js/document-management',
                    mainConfigFile: '<%= yeoman.app %>/document-management/main.js',
                    out: '<%= yeoman.dist %>/document-management/main.js',
                    paths:{localization: 'empty:'},
                    findNestedDependencies: true
                }
            },

            productManagement: {
                options: {
                    name: '../../product-management/main',
                    optimize: 'none',
                    preserveLicenseComments: false,
                    useStrict: true,
                    wrap: true,
                    inlineText: true,
                    baseUrl: '<%= yeoman.app %>/js/product-management',
                    mainConfigFile: '<%= yeoman.app %>/product-management/main.js',
                    out: '<%= yeoman.dist %>/product-management/main.js',
                    paths:{localization: 'empty:'},
                    findNestedDependencies: true
                }

            },

            productStructure: {
                options: {
                    name: '../../product-structure/main',
                    optimize: 'none',
                    preserveLicenseComments: false,
                    useStrict: true,
                    wrap: true,
                    inlineText: true,
                    baseUrl: '<%= yeoman.app %>/js/product-structure',
                    mainConfigFile: '<%= yeoman.app %>/product-structure/main.js',
                    out: '<%= yeoman.dist %>/product-structure/main.js',
                    paths:{localization: 'empty:'},
                    findNestedDependencies: true
                }
            },

            productFrame: {
                options: {
                    name: '../../visualization/main',
                    optimize: 'none',
                    preserveLicenseComments: false,
                    useStrict: true,
                    wrap: true,
                    inlineText: true,
                    baseUrl: '<%= yeoman.app %>/js/product-structure',
                    mainConfigFile: '<%= yeoman.app %>/visualization/main.js',
                    out: '<%= yeoman.dist %>/visualization/main.js',
                    paths:{localization: 'empty:'},
                    findNestedDependencies: true
                }
            },

            changeManagement: {
                options: {
                    name: '../../change-management/main',
                    optimize: 'none',
                    preserveLicenseComments: false,
                    useStrict: true,
                    wrap: true,
                    inlineText: true,
                    baseUrl: '<%= yeoman.app %>/js/change-management',
                    mainConfigFile: '<%= yeoman.app %>/change-management/main.js',
                    out: '<%= yeoman.dist %>/change-management/main.js',
                    paths:{localization: 'empty:'},
                    findNestedDependencies: true
                }
            }

        },
        uglify: {
            documentManagement: {
                files: {
                    '<%= yeoman.dist %>/document-management/main.js': ['<%= yeoman.dist %>/document-management/main.js']
                }
            },
            productManagement: {
                files: {
                    '<%= yeoman.dist %>/product-management/main.js': ['<%= yeoman.dist %>/product-management/main.js']
                }
            },
            productStructure: {
                files: {
                    '<%= yeoman.dist %>/product-structure/main.js': ['<%= yeoman.dist %>/product-structure/main.js']
                }
            },
            productFrame: {
                files: {
                    '<%= yeoman.dist %>/visualization/main.js': ['<%= yeoman.dist %>/visualization/main.js']
                }
            },
            changeManagement: {
                files: {
                    '<%= yeoman.dist %>/change-management/main.js': ['<%= yeoman.dist %>/change-management/main.js']
                }
            }
        },
        less: {
            dist: {
                options: {
                    strictImports: false,
                    paths: [
                        '<%= yeoman.app %>/less/document-management/',
                        '<%= yeoman.app %>/less/product-management/',
                        '<%= yeoman.app %>/less/product-structure/',
                        '<%= yeoman.app %>/less/change-management/'
                    ]
                },
                files: {
                    '<%= yeoman.app %>/document-management/main.css': '<%= yeoman.app %>/less/document-management/style.less',
                    '<%= yeoman.app %>/product-management/main.css': '<%= yeoman.app %>/less/product-management/style.less',
                    '<%= yeoman.app %>/product-structure/main.css': '<%= yeoman.app %>/less/product-structure/style.less',
                    '<%= yeoman.app %>/visualization/main.css': '<%= yeoman.app %>/less/product-structure/style_frame.less',
                    '<%= yeoman.app %>/change-management/main.css': '<%= yeoman.app %>/less/change-management/style.less'
                }
            }
        },
        cssmin: {
            options: {
                keepSpecialComments: 0
            },
            documentManagement: {
                files: {
                    '<%= yeoman.dist %>/document-management/main.css': ['<%= yeoman.app %>/document-management/main.css']
                }
            },
            productManagement: {
                files: {
                    '<%= yeoman.dist %>/product-management/main.css': ['<%= yeoman.app %>/product-management/main.css']
                }
            },
            productStructure: {
                files: {
                    '<%= yeoman.dist %>/product-structure/main.css': ['<%= yeoman.app %>/product-structure/main.css']
                }
            },
            productFrame: {
                files: {
                    '<%= yeoman.dist %>/visualization/main.css': ['<%= yeoman.app %>/visualization/main.css']
                }
            },
            changeManagement: {
                files: {
                    '<%= yeoman.dist %>/change-management/main.css': ['<%= yeoman.app %>/change-management/main.css']
                }
            }
        },
        usemin: {
            documentManagement: {
                html: ['<%= yeoman.dist %>/document-management/index.html'],
                css: ['<%= yeoman.dist %>/document-management/main.css'],
                options: {
                    dirs: ['<%= yeoman.dist %>/document-management']
                }
            },
            productManagement: {
                html: ['<%= yeoman.dist %>/product-management/index.html'],
                css: ['<%= yeoman.dist %>/product-management/main.css'],
                options: {
                    dirs: ['<%= yeoman.dist %>/product-management']
                }
            },
            productStructure: {
                html: ['<%= yeoman.dist %>/product-structure/index.html'],
                css: ['<%= yeoman.dist %>/product-structure/main.css'],
                options: {
                    dirs: ['<%= yeoman.dist %>/product-structure']
                }
            },
            productFrame: {
                html: ['<%= yeoman.dist %>/visualization/index.html'],
                css: ['<%= yeoman.dist %>/visualization/main.css'],
                options: {
                    dirs: ['<%= yeoman.dist %>/visualization']
                }
            },
            changeManagement: {
                html: ['<%= yeoman.dist %>/change-management/index.html'],
                css: ['<%= yeoman.dist %>/change-management/main.css'],
                options: {
                    dirs: ['<%= yeoman.dist %>/change-management']
                }
            }
        },
        htmlmin: {
            documentManagement: {
                files: [
                    {
                        expand: true,
                        cwd: '<%= yeoman.app %>/document-management',
                        src: 'index.html',
                        dest: '<%= yeoman.dist %>/document-management'
                    }
                ]
            },
            productManagement: {
                files: [
                    {
                        expand: true,
                        cwd: '<%= yeoman.app %>/product-management',
                        src: 'index.html',
                        dest: '<%= yeoman.dist %>/product-management'
                    }
                ]
            },
            productStructure: {
                files: [
                    {
                        expand: true,
                        cwd: '<%= yeoman.app %>/product-structure',
                        src: 'index.html',
                        dest: '<%= yeoman.dist %>/product-structure'
                    }
                ]
            },
            productFrame: {
                files: [
                    {
                        expand: true,
                        cwd: '<%= yeoman.app %>/visualization',
                        src: 'index.html',
                        dest: '<%= yeoman.dist %>/visualization'
                    }
                ]
            },
            changeManagement: {
                files: [
                    {
                        expand: true,
                        cwd: '<%= yeoman.app %>/change-management',
                        src: 'index.html',
                        dest: '<%= yeoman.dist %>/change-management'
                    }
                ]
            }
        },
        copy: {
            libs: {
                files: [
                    {
                        expand: true,
                        dot: false,
                        cwd: '<%= yeoman.app %>',
                        dest: '<%= yeoman.dist %>',
                        src: [
                            'bower_components/requirejs/require.js',
                            'bower_components/modernizr/modernizr.js',
                            'bower_components/jquery/jquery.min.*',
                            'bower_components/underscore/underscore-min.js',
                            'bower_components/threejs/build/three.min.js',
                            'bower_components/tweenjs/build/tween.min.js',
                            'bower_components/bootstrap/docs/assets/js/bootstrap.min.js',
                            'bower_components/backbone/backbone-min.js'
                        ]
                    }
                ]
            },
            assets: {
                files: [
                    {
                        expand: true,
                        dot: false,
                        cwd: '<%= yeoman.app %>',
                        dest: '<%= yeoman.dist %>',
                        src: [
                            'css/**',
                            'images/**',
                            'sounds/**',
                            'fonts/**',
                            'download/**',
	                        'js/home/main.js',
	                        'js/lib/plugin-detect.js',
	                        'js/lib/empty.pdf',
	                        'js/lib/charts/**'
                        ]
                    },{
                        expand: true,
                        dot: false,
                        cwd: '<%= yeoman.app %>/bower_components/bootstrap/',
                        dest: '<%= yeoman.dist %>',
                        src: [
                            'img/*'
                        ]
                    }
                ]
            },
            dmu: {
                files: [
                    {
                        expand: true,
                        dot: false,
                        cwd: '<%= yeoman.app %>',
                        dest: '<%= yeoman.dist %>',
                        src: [
                            /*
                             * worker utils
                            * */
                            'js/product-structure/workers/*',
                            'js/product-structure/dmu/loaders/*',
                            'js/product-structure/dmu/utils/*',
                            'js/product-structure/dmu/controls/*',
                            'js/product-structure/permalinkApp.js'
                        ]
                    }
                ]
            },
            i18n:{
                files: [
                    {
                        expand: true,
                        dot: false,
                        cwd: '<%= yeoman.app %>',
                        dest: '<%= yeoman.dist %>',
                        src: [
                            'js/localization/nls/*',
                            'js/localization/nls/fr/*',
                            'js/localization/nls/es/*'
                        ]
                    }
                ]
            },

            webapp: {
                files: [
                    {
                        expand: true,
                        dot: false,
                        cwd: '<%= yeoman.dist %>',
                        dest: '<%= yeoman.webapp %>',
                        src: [
                            '**'
                        ]
                    }
                ]
            }
        },
        jshint: {
            options: {
                jshintrc: '.jshintrc',
                reporter: require('jshint-stylish')
            },
            all: {
                src:[
                    'Gruntfile.js',
                    '<%= yeoman.app %>/js/**/*.js',
                    '<%= yeoman.tests %>/js/**/*.js',
                    '!<%= yeoman.app %>/js/lib/**',
                    '!<%= yeoman.app %>/js/product-structure/dmu/utils/**',
                    '!<%= yeoman.app %>/js/product-structure/dmu/loaders/**',
                    '!<%= yeoman.app %>/js/product-structure/dmu/controls/**',
                    '!<%= yeoman.app %>/js/localization/**'
                ]
            }
        },
        execute:{
            tests:{
                options:{
                    cwd:'tests'
                },
                src:['tests/run.js']
            }
        }
    });

	grunt.loadTasks('tasks');
};
