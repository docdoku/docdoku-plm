var chai = require('chai');
var expect = chai.expect;
var DocdokuPlmClient = require('../lib/docdoku-api');
var config = require('./test.config');

describe('Api auth tests', function() {

    var jwt, cookie;

    it('Should login and get session id and jwt', function(done) {

        var client = new DocdokuPlmClient({url:config.url});
        client.getApi().then(function(api){
            api.auth.login({body:{login:config.login,password:config.password}}).then(function(response){
                var headers = response.headers;
                jwt = headers.jwt;
                cookie = headers['set-cookie'][0];
                expect(jwt).to.be.defined;
                expect(cookie).to.be.defined;
                done();
            });
        });
    });
/*

    it('Should be able tu use basic auth', function(done) {

        var client = new DocdokuPlmClient({
            url:config.url,
            login:config.login,
            password:config.password,
            basicAuth:true
        });

        client.getApi().then(function(api){
            api.accounts.getAccount().then(function(response){
                expect(response.status,200);
                done();
            });
        });
    });*/

    it('Should be able tu use cookie', function(done) {

        var client = new DocdokuPlmClient({
            url:config.url,
            cookie:cookie
        });

        client.getApi().then(function(api){
            api.accounts.getAccount().then(function(response){
                expect(response.status,200);
                done();
            });
        });
    });



});
