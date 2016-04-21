module.exports = function(grunt) {
	'use strict';
	/*
	 * Main build : grunt build
	 * */
	grunt.registerTask('build', [
		'clean:dist',
		'less',
		'copy:libs',
		'copy:assets',
		'copy:dmu',
		'copy:i18n',
		'build-module:documentManagement',
		'build-module:productManagement',
		'build-module:productStructure',
		'build-module:productFrame',
		'build-module:changeManagement'
	]);
};