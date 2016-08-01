var config = require('yargs').argv;

var defaults = {
    url :'http://localhost:8080/api',
    login :'test',
    password:'test',
    workspace:'test'
};

config.login = config.login ? config.login : defaults.login;
config.password = config.password ? config.password : defaults.password;
config.workspace = config.workspace ? config.workspace : defaults.workspace;
config.url = config.url ? config.url : defaults.url;

module.exports = config;