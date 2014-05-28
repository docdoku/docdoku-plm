/*
 * grunt-markdown-pdf
 * https://github.com/alan/shaw/grunt-markdown-pdf
 *
 * Copyright (c) 2013 Alan Shaw
 * Licensed under the MIT license.
 */

module.exports = function(grunt) {

  // Project configuration.
  grunt.initConfig({

    // Before generating any new files, remove any previously-created files.
    clean: {
      tests: ['tmp']
    },

    // Configuration to be run (and then tested).
    markdownpdf: {
      all: {
        src: "test/fixtures/**/*",
        dest: "tmp/all"
      },
      some0: {
        files: {
          "tmp/some0": "test/fixtures/test0.md"
        }
      },
      some1: {
        files: {
          "tmp/some1": "test/fixtures/test/*"
        }
      },
      concat: {
        options: {concat: true},
        src: "test/fixtures/**/*",
        dest: "tmp/concatenated.pdf"
      }
    },

    // Unit tests.
    nodeunit: {
      tests: ['test/*_test.js']
    }

  })

  // Actually load this plugin's task(s).
  grunt.loadTasks('tasks')

  // These plugins provide necessary tasks.
  grunt.loadNpmTasks('grunt-contrib-clean')
  grunt.loadNpmTasks('grunt-contrib-nodeunit')

  // Whenever the "test" task is run, first clean the "tmp" dir, then run this
  // plugin's task(s), then test the result.
  grunt.registerTask('test', ['clean', 'markdownpdf', 'nodeunit'])

  // By default, run all tests.
  grunt.registerTask('default', ['test'])

}
