module.exports = function(grunt) {
	'use strict';
	/*
	 * Dev mode : grunt serve (needs proxy like nginx or haproxy)
	 * Dev mode, ventilated sources : grunt serve
	 * Dev mode, ventilated sources, without live reloading : grunt serve:noLiveReload
	 * Try the build: grunt serve:dist
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
                'open:dev'
            ]);
		}

        // Default serve command

        grunt.task.run([
			'clean:server',
			'less',
			'connect:livereload',
			'open:dev',
			'watch:dev'
		]);

	});

    // On watch events, configure jshint to only run on changed file, run less task if file is a less source file

    var filesTask = {
        less:function(filepath){
            grunt.task.run(['less']);
        },
        js:function(filepath){
            grunt.config('jshint.all.src', filepath);
            grunt.task.run(['jshint']);
        }
    };

    grunt.event.on('watch', function(action, filepath) {
        var extension = filepath.substr((~-filepath.lastIndexOf(".") >>> 0) + 2);
        if(filesTask[extension]){
            filesTask[extension](filepath);
        }
    });

};
