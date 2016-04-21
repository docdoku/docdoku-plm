module.exports = function(grunt) {
	'use strict';
	/*
	 * Module build : grunt build-module:documentManagement
	 * */
	grunt.registerTask('build-module', function (module) {
        return grunt.task.run([
            'clean:'+module,
            'requirejs:'+module,
            'uglify:'+module,
            'cssmin:'+module,
            'htmlmin:'+module,
            'usemin:'+module
        ]);
	});
};
