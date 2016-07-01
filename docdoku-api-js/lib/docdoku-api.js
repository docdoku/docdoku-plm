var SwaggerClient = require('swagger-client');
var spec = require('./spec');

var DocdokuPLMClient = module.exports = function(options){
    this.setOptions(options);
};

DocdokuPLMClient.prototype.setOptions = function(options){
    this.options = options || {};
    return this;
};

DocdokuPLMClient.prototype.setBasicAuth = function(login,password){
    this.options.authorizations = {
        easyapi_basic: new SwaggerClient.PasswordAuthorization(login,password)
    };
    return this;
};

DocdokuPLMClient.prototype.removeAuth = function(){
    this.options.authorizations = {};
    return this;
};

DocdokuPLMClient.prototype.getApi = function(){
    this.client = new SwaggerClient({
        url: this.options.url,
        spec:spec,
        usePromise:true,
        authorizations:this.authorizations
    });
    return this.client;
};
