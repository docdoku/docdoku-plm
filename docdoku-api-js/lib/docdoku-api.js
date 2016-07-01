var SwaggerClient = require('swagger-client');
var auth = require('swagger-client/lib/auth');
var spec = require('./spec');

var DocdokuPLMClient = module.exports = function(options){
    this.setOptions(options);
};

DocdokuPLMClient.prototype.setOptions = function(options){

    this.options = options || {};
    this.authorizations = this.options.authorizations || {};

    if(this.options.basicAuth){
        this.authorizations.basicAuth = new SwaggerClient.PasswordAuthorization(this.options.login,this.options.password);
    }

    if(this.options.cookie){
        this.authorizations.cookieAuth = new SwaggerClient.ApiKeyAuthorization('Cookie', this.options.cookie, 'header');
    }

    if(this.options.jwt){
        this.authorizations.jwtAuth = new SwaggerClient.ApiKeyAuthorization('Authorization', 'Bearer ' + this.options.jwt, 'header');
    }

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
