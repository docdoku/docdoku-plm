module.exports = function(grunt){

    'use strict';

    require('load-grunt-tasks')(grunt);

    var nwjsVersion = '0.20.0-rc1';
    var platforms = ['linux32','linux64','osx64','win32','win64'];

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
                    angular: true
                },
                reporterOutput: ''
            }
        },

        clean:  {
            dist: ['tmp', 'dist', 'target']
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
                            'img/**'
                        ],
                        dest: 'dist/'
                },{
                    expand: true,
                    flatten: true,
                    cwd: 'app/',
                    src: [
                        'bower_components/material-design-icons/iconfont/MaterialIcons*'
                    ],
                    dest: 'dist'
                }]
            }
        },

        concat: {
            vendor:{
                src:[
                    'app/bower_components/angular/angular.min.js',
                    'app/bower_components/threejs/build/three.min.js',
                    'app/bower_components/angular-aria/angular-aria.min.js',
                    'app/bower_components/hammerjs/hammer.min.js',
                    'app/bower_components/angular-animate/angular-animate.min.js',
                    'app/bower_components/angular-material/angular-material.min.js',
                    'app/bower_components/angular-route/angular-route.min.js',
                    'app/bower_components/angular-translate/angular-translate.min.js',
                    'app/bower_components/angular-uuid4/angular-uuid4.min.js',
                    'app/bower_components/angular-material-data-table/dist/md-data-table.min.js'
                ],
                dest:'dist/vendor.js'
            },
            app: {
                src: ['app/modules.js', 'app/js/**/*.js', 'tmp/templates.js'],
                dest: 'dist/app.js'
            },
            css:{
                src:[
                    'app/bower_components/angular-material/angular-material.css',
                    'app/bower_components/angular-material-data-table/dist/md-data-table.css',
                    'app/bower_components/material-design-icons/iconfont/material-icons.css',
                    'app/css/*.css'
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

        nwjs: {
            options: {
                version: nwjsVersion,
                platforms: ['linux', 'win', 'osx64'],
                buildDir: 'target',
            },
            src: ['dist/**/*']
        },

        compress:{
            linux32: {
                options: {
                    archive: 'target/dplm-linux-32.zip'
                },
                files: [
                    {expand: true, cwd: 'target/DPLM/linux32', src: ['**'], dest:''}
                ]
            },
            linux64: {
                options: {
                    archive: 'target/dplm-linux-64.zip'
                },
                files: [
                    {expand: true, cwd: 'target/DPLM/linux64', src: ['**'], dest:''}
                ]
            },
            osx64: {
                options: {
                    archive: 'target/dplm-osx-64.zip'
                },
                files: [
                    {expand: true, cwd: 'target/DPLM/osx64', src: ['**'], dest:''}
                ]
            },
            win32: {
                options: {
                    archive: 'target/dplm-win-32.zip'
                },
                files: [
                    {expand: true, cwd: 'target/DPLM/win32', src: ['**'], dest:''}
                ]
            },
            win64: {
                options: {
                    archive: 'target/dplm-win-64.zip'
                },
                files: [
                    {expand: true, cwd: 'target/DPLM/win64', src: ['**'], dest:''}
                ]
            }
        },
        exec: {
            linux32: 'cache/'+nwjsVersion+'/linux32/nw app',
            linux64: 'cache/'+nwjsVersion+'/linux64/nw app',
            osx64: 'cache/'+nwjsVersion+'/osx64/nw app',
            win32: 'cache/'+nwjsVersion+'/win32/nw.exe app',
            win64: 'cache/'+nwjsVersion+'/osx64/nw.exe app',
            dist_linux32: 'cache/'+nwjsVersion+'/linux32/nw dist',
            dist_linux64: 'cache/'+nwjsVersion+'/linux64/nw dist',
            dist_osx64: 'cache/'+nwjsVersion+'/osx64/nw dist',
            dist_win32: 'cache/'+nwjsVersion+'/win32/nw.exe dist',
            dist_win64: 'cache/'+nwjsVersion+'/osx64/nw.exe dist'
        }
    });

    grunt.registerTask('build',[
        'jshint',
        'clean:dist',
        'copy:dist',
        'html2js:dist',
        'concat:vendor',
        'concat:app',
        'concat:css',
        'ngAnnotate:dist',
        'uglify:dist',
        'cssmin:dist',
        'usemin',
        'nwjs',
        'compress'
    ]);



    grunt.registerTask('dev',function(platform, useDist){
        if(!platform || platforms.indexOf(platform) === -1){
            return grunt.fail.warn('Usage : `grunt dev:<platform>` \nAvailable platforms : ' + platforms.join(', '));
        }else{
            return grunt.task.run('exec:'+ (useDist ? 'dist_' : '') + platform);
        }
    });

};