'use strict';
var moduleName = 'serverFront';

var LIVERELOAD_PORT = 35730;
var SERVER_HOSTNAME = 'localhost';
var SERVER_PORT = 9001;
var DEV_HOSTNAME = 'localhost';
var DEV_PORT = 8989;
var lrSnippet = require('connect-livereload')({port: LIVERELOAD_PORT});

var mountFolder = function (connect, dir) {
    return connect.static(require('path').resolve(dir));
};

module.exports = {

    name: moduleName,

    loadConf: function (config, grunt) {

        config.watch = {
            dev: {
                options: {
                    nospawn: true,
                    livereload: LIVERELOAD_PORT
                },
                files: [
                    'Gruntfile.js',
                    '{app,grunt}/**/*',
                    '!app/less/**/*'
                ]
            },
            tests: {
                files: ['tests/js/**/*.js'],
                tasks: ['execute:tests', 'watch:tests']
            },
            less: {
                files: ['app/less/**/*.less'],
                tasks: ['less', 'watch:less']
            }
        };

        config.connect = {
            options: {
                port: SERVER_PORT,
                hostname: SERVER_HOSTNAME
            },

            livereload: {
                options: {
                    middleware: function (connect) {
                        return [
                            lrSnippet,
                            mountFolder(connect, '.tmp'),
                            mountFolder(connect, 'app')
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
            },
            app: {
                options: {
                    middleware: function (connect) {
                        return [
                            mountFolder(connect, 'app')
                        ];
                    }
                }
            }
        };

        config.open = {
            server: {
                path: 'http://localhost:<%= connect.options.port %>'
            },
            dev: {
                path: 'http://' + DEV_HOSTNAME + ':' + DEV_PORT
            }
        };

        config.clean.server = '.tmp';
    },

    loadTasks: function (grunt) {

        grunt.registerTask('serve', function (target) {

            if (target === 'dist') {
                return grunt.task.run(['build', 'open:server', 'connect:dist:keepalive']);
            }

            if (target === 'dist-no-build') {
                return grunt.task.run(['open:server', 'connect:dist:keepalive']);
            }

            if (target === 'less') {
                return grunt.task.run(['less','watch:less']);
            }

            if (target === 'tests') {
                return grunt.task.run(['execute:tests','watch:tests']);
            }

            if (target === 'noLiveReload') {
                return grunt.task.run([
                    'clean:server',
                    'less',
                    'connect:app',
                    'open:dev'
                ]);
            }

            // Default serve command

            grunt.task.run([
                'clean:server',
                'less',
                'connect:livereload',
                'open:dev',
                'watch:dev'
            ]);

        });

        // On watch events, configure jshint to only run on changed file, run less task if file is a less source file

        var filesTask = {
            js: function (filepath) {
                grunt.config('jshint.current.src', filepath);
                grunt.task.run(['jshint:current']);
            }
        };

        grunt.event.on('watch', function (action, filepath) {
            var extension = filepath.substr((~-filepath.lastIndexOf('.') >>> 0) + 2);
            if (typeof filesTask[extension] === 'function') {
                filesTask[extension](filepath);
            }
        });

    }

};
