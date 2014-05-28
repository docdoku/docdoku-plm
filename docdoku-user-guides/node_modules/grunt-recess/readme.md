# grunt-recess

> Lint and minify CSS and LESS, using the Twitter [RECESS][recess] module.

#### Problems with the output should be submitted on Recess [issue tracker](https://github.com/twitter/recess/issues).


## Getting Started

If you haven't used [grunt][] before, be sure to check out the [Getting Started][] guide, as it explains how to create a [gruntfile][Getting Started] as well as install and use grunt plugins. Once you're familiar with that process, install this plugin with this command:

```shell
npm install --save-dev grunt-recess
```

Once the plugin has been installed, it may be enabled inside your Gruntfile with this line of JavaScript:

```js
grunt.loadNpmTasks('grunt-recess');
```

*Tip: the [load-grunt-tasks](https://github.com/sindresorhus/load-grunt-tasks) module makes it easier to load multiple grunt tasks.*

[grunt]: http://gruntjs.com
[Getting Started]: https://github.com/gruntjs/grunt/wiki/Getting-started


## Documentation


### Example usage


#### Lint

```javascript
recess: {
	dist: {
		src: ['src/main.css']
	}
}
```


#### Lint and compile

```javascript
recess: {
	dist: {
		options: {
			compile: true
		},
		files: {
			'dist/main.css': ['src/main.less']
		}
	}
}
```

A destination is only needed when `compile: true`. It won't output any warnings in this mode.
You can also specify `.less` files and they will be compiled.


#### Lint, compile and concat

```javascript
recess: {
	dist: {
		options: {
			compile: true
		},
		files: {
			'dist/combined.css': [
				'src/main.css',
				'src/component.css'
			]
		}
	}
}
```

You can specify multiple source files to concat them.


### Options

```javascript
// Default
compile: false 				// Compiles CSS or LESS. Fixes white space and sort order.
compress: false				// Compress your compiled code
noIDs: true					// Doesn't complain about using IDs in your stylesheets
noJSPrefix: true			// Doesn't complain about styling .js- prefixed classnames
noOverqualifying: true		// Doesn't complain about overqualified selectors (ie: div#foo.bar)
noUnderscores: true			// Doesn't complain about using underscores in your class names
noUniversalSelectors: true	// Doesn't complain about using the universal * selector
prefixWhitespace: true		// Adds whitespace prefix to line up vender prefixed properties
strictPropertyOrder: true	// Complains if not strict property order
zeroUnits: true				// Doesn't complain if you add units to values of 0
includePath: mixed			// Additional paths to look for `@import`'ed LESS files.  Accepts a string or an array of strings.
```


## gulp

You might want to check out [gulp-recess](https://github.com/sindresorhus/gulp-recess) which is a faster alternative to this task.


## License

MIT © [Sindre Sorhus](http://sindresorhus.com)


[recess]: https://github.com/twitter/recess
