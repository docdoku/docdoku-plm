var grunt = require('grunt')

exports.markdownpdf = {
  all: function(test) {
    test.expect(2)

    var contents0 = grunt.file.read('tmp/all/test0.pdf')

    test.ok(contents0.length > 0)

    var contents1 = grunt.file.read('tmp/all/test1.pdf')

    test.ok(contents1.length > 0)

    test.done()
  },
  some0: function(test) {
    test.expect(1)

    var contents = grunt.file.read('tmp/some0/test0.pdf')

    test.ok(contents.length > 0)

    test.done()
  },
  some1: function(test) {
    test.expect(1)

    var contents = grunt.file.read('tmp/some1/test1.pdf')

    test.ok(contents.length > 0)

    test.done()
  },
  concatenated: function(test) {
    test.expect(1)

    var contents = grunt.file.read('tmp/concatenated.pdf')

    test.ok(contents.length > 0)

    test.done()
  }
}
