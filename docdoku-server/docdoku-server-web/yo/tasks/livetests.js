module.exports = function(grunt) {
	'use strict';
	grunt.registerTask('livetests',['execute:tests','watch:tests']);
};