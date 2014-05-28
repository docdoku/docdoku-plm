# grunt-markdown-pdf [![Build Status](https://travis-ci.org/alanshaw/grunt-markdown-pdf.png)](https://travis-ci.org/alanshaw/grunt-markdown-pdf) [![dependency Status](https://david-dm.org/alanshaw/grunt-markdown-pdf.png)](https://david-dm.org/alanshaw/grunt-markdown-pdf)

> Grunt plugin to convert markdown documents to PDF

Thin wrapper around [markdown-pdf](https://github.com/alanshaw/markdown-pdf).

The PDF looks great because it is styled by HTML5 Boilerplate. What? - Yes! Your Markdown is first converted to HTML, then pushed into the HTML5 Boilerplate `index.html`. Phantomjs renders the page and saves it to a PDF. You can even customise the style of the PDF by passing an optional path to your CSS _and_ you can pre-process your markdown file before it is converted to a PDF by passing in a pre-processing function, for templating.

## Getting Started
This plugin requires Grunt `~0.4.1`

If you haven't used [Grunt](http://gruntjs.com/) before, be sure to check out the [Getting Started](http://gruntjs.com/getting-started) guide, as it explains how to create a [Gruntfile](http://gruntjs.com/sample-gruntfile) as well as install and use Grunt plugins. Once you're familiar with that process, you may install this plugin with this command:

```shell
npm install grunt-markdown-pdf --save-dev
```

One the plugin has been installed, it may be enabled inside your Gruntfile with this line of JavaScript:

```js
grunt.loadNpmTasks('grunt-markdown-pdf');
```

## The "markdownpdf" task

### Overview
In your project's Gruntfile, add a section named `markdownpdf` to the data object passed into `grunt.initConfig()`.

```js
grunt.initConfig({
  markdownpdf: {
    options: {
      // Task-specific options go here.
    },
    your_target: {
      // Target-specific file lists and/or options go here.
    },
  }
});
```

### Options

#### options.phantomPath
Type: `String`
Default value: `Path provided by phantomjs module`

Path to phantom binary

#### options.concat
Type: `Boolean`
Default value: `false`

If set to true, a single PDF will be created containing the contents of all of the Markdown files.

#### options.cssPath
Type: `String`
Default value: `node_modules/markdown-pdf/pdf.css`

Path to custom CSS file, relative to the current working directory.

#### options.paperFormat
Type: `String`
Default value: `A4`

'A3', 'A4', 'A5', 'Legal', 'Letter' or 'Tabloid'

#### options.paperOrientation
Type: `String`
Default value: `portrait`

'portrait' or 'landscape'

#### options.paperBorder
Type: `String`
Default value: `1cm`

Supported dimension units are: 'mm', 'cm', 'in', 'px'

#### options.renderDelay
Type: `Number`
Default value: `1000`

Delay in millis before rendering the PDF (give HTML and CSS a chance to load)

#### options.preProcessMd
Type: `Function`
Default value: `function () { return through() }`

A function that returns a [through stream](https://npmjs.org/package/through) that transforms the markdown before it is converted to HTML.

#### options.preProcessHtml
Type: `Function`
Default value: `function () { return through() }`

A function that returns a [through stream](https://npmjs.org/package/through) that transforms the HTML before it is converted to markdown.

### Usage Examples

#### Default Options
In this example, the default options are used to convert all markdown files in the directory `src/` to PDFs in the directory `dest/`.

```js
grunt.initConfig({
  markdownpdf: {
    options: {},
    files: {
      src: "src/*.md",
      dest: "dest"
    }
  }
})
```

### Replace characters with preProcessMd
In this example we use a through stream called [split](https://npmjs.org/package/split) to split the markdown file into lines and replace `foo` with `bar`.

```js
var split = require("split")

grunt.initConfig({
  markdownpdf: {
    dist: {
      options: {
        preProcessMd: function () {
          return split(function (line) {
            return line.replace("foo", "bar") + "\n"
          })
        }
      },
      src: "document.md",
      dest: "dist/"
    }
  }
})
```

## Contributing
In lieu of a formal styleguide, take care to maintain the existing coding style. Add unit tests for any new or changed functionality. Lint and test your code using [Grunt](http://gruntjs.com/).

## Release History
 * 2013-12-27   v2.0.0   Use updated (streaming) markdown-pdf module and implement concat files properly
 * 2013-09-04   v1.0.0   Use updated markdown-pdf module - CSS path is now relative to current working directory
 * 2013-06-14   v0.3.0   Use marked module for better markdown compatibility and performance
 * 2013-05-30   v0.2.0   Allow concatenation of multiple source files to produce a single PDF
 * 2013-05-19   v0.1.0   Callback hooks allowing markdown (and html) content to be pre-processed
 * 2013-05-19   v0.0.0   Initial release
