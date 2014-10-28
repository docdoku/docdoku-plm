module.exports = function(grunt) {
	'use strict';
	/*
	 * Dev mode : grunt serve (needs proxy like nginx)
	 * Dev mode with build : grunt serve:dist
	 * */
	grunt.registerTask('serve', function (target) {
		if (target === 'dist') {
			return grunt.task.run(['build', 'open:server', 'connect:dist:keepalive']);
		}
		if (target === 'noLiveReload') {
			return grunt.task.run([
                'clean:server',
                'less',
                'connect:app',
                'open:dev',
                'watch:livereload',
                'watch:less'
                ]);
		}

		grunt.task.run([
			'clean:server',
			'less',
			'connect:livereload',
			'open:dev',
			'watch:livereload',
			'watch:less'
		]);
	});

};
