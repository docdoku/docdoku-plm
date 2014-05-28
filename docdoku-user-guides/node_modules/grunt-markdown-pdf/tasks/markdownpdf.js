/*
 * grunt-markdown-pdf
 * https://github.com/alanshaw/grunt-markdown-pdf
 *
 * Copyright (c) 2013 Alan Shaw
 * Licensed under the MIT license.
 */

var markdownpdf = require("markdown-pdf")
  , path = require("path")
  , async = require("async")

module.exports = function (grunt) {

  grunt.registerMultiTask("markdownpdf", "Convert Markdown documents to PDF", function () {

    var opts = this.options()
      , done = this.async()

    // Create the tasks to process the targets
    var tasks = this.files.map(function (f) {

      return function (cb) {

        var srcs = f.src.filter(function (filepath) {
          // Warn on and remove invalid source files (if nonull was set).
          if (!grunt.file.exists(filepath)) {
            grunt.log.warn('Source file not found ' + filepath)
            return false
          }
          if(!grunt.file.isFile(filepath)) {
            grunt.verbose.writeln('Ignoring non file ' + filepath)
            return false
          }
          grunt.verbose.writeln("Found src file: " + filepath)
          return true
        })

        if (opts.concat) {

          markdownpdf(opts).concat.from(srcs).to(f.dest, function (er) {
            if (er) return cb(er)
            cb(null, [f.dest])
          })

        } else {

          var dests = srcs.map(function (src) {
            var destPath = path.join(f.dest, path.basename(src).replace(/\.(markdown|md)/g, "") + ".pdf")
            grunt.verbose.writeln("Determined dest path: " + destPath)
            return destPath
          })

          markdownpdf(opts).from(srcs).to(dests, function (er) {
            if (er) return cb(er)
            cb(null, dests)
          })
        }
      }
    })

    async.parallel(tasks, function (er, targetDestPaths) {
      if (er) return grunt.warn(er)

      targetDestPaths.forEach(function (destPaths) {
        destPaths.forEach(function(destPath) {
          grunt.log.ok(destPath)
        })
      })

      done()
    })
  })
}
