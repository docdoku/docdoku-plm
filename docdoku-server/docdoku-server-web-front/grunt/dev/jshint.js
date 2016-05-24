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
                    'app/js/**/*.js',
                    'tests/js/**/*.js',
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
