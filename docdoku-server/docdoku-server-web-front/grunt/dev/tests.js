var moduleName = 'tests';

module.exports = {

    name:moduleName,

    loadConf:function(config,grunt){
        config.execute = {
            tests:{
                options:{
                    cwd:'tests'
                },
                src:['tests/run.js']
            }
        };
    },

    loadTasks:function(grunt){
        grunt.registerTask('test',['execute:tests']);
    }

};
