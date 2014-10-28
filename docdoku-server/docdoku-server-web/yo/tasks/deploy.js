module.exports = function(grunt) {
	'use strict';
	/*
	 * Deploy command : used by maven
	 * */
	grunt.registerTask('deploy',['clean:webapp','build','copy:webapp','clean:dist']);
};