module.exports = {
    loadConf:function(config, grunt){
        config.jshint= {
            options: {
                jshintrc: '.jshintrc',
                    reporter: require('jshint-stylish')
            },
            all: {
                src:[
                    'Gruntfile.js',
                    'app/**/*.js',
                    'tests/js/**/*.js',
                    '!app/bower_components/**',
                    '!app/js/lib/**',
                    '!app/js/dmu/**'
                ]
            },
            current:{}
        };
    },
    loadTasks:function(grunt){

    }
};
