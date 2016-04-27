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
                    '!app/js/product-structure/dmu/utils/**',
                    '!app/js/product-structure/dmu/loaders/**',
                    '!app/js/product-structure/dmu/controls/**',
                    '!app/js/localization/**'
                ]
            },
            current:{}
        };
    },
    loadTasks:function(grunt){

    }
};
