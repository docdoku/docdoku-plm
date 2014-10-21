module.exports = function(grunt) {
	'use strict';
	/*
	 * Module build : grunt build-module:documentManagement
	 * */
	grunt.registerTask('build-module', function (module) {

		if(module === 'documentManagement'){
			return grunt.task.run([
				'clean:documentManagement',
				'requirejs:documentManagement',
				'uglify:documentManagement',
				'cssmin:documentManagement',
				'htmlmin:documentManagement',
				'usemin:documentManagement'
			]);
		}

		if(module === 'productManagement'){
			return grunt.task.run([
				'clean:productManagement',
				'requirejs:productManagement',
				'uglify:productManagement',
				'cssmin:productManagement',
				'htmlmin:productManagement',
				'usemin:productManagement'
			]);
		}

		if(module === 'productStructure'){
			return grunt.task.run([
				'clean:productStructure',
				'requirejs:productStructure',
				'uglify:productStructure',
				'cssmin:productStructure',
				'htmlmin:productStructure',
				'usemin:productStructure'
			]);
		}

		if(module === 'changeManagement'){
			return grunt.task.run([
				'clean:changeManagement',
				'requirejs:changeManagement',
				'uglify:changeManagement',
				'cssmin:changeManagement',
				'htmlmin:changeManagement',
				'usemin:changeManagement'
			]);
		}

		if(module === 'productFrame'){
			return grunt.task.run([
				'clean:productFrame',
				'requirejs:productFrame',
				'uglify:productFrame',
				'cssmin:productFrame',
				'htmlmin:productFrame',
				'usemin:productFrame'
			]);
		}

	});
};