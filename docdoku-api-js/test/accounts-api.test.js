var chai = require('chai');
var expect = chai.expect;
var DocdokuPlmClient = require('../lib/docdoku-api');
var config = require('./test.config');

describe('Accounts api tests', function() {

    it('Should create an account', function(done){

        var client = new DocdokuPlmClient({url:config.url});

        client.getApi().then(function(api){

            expect(api.apis.accounts).to.be.defined;
            expect(api.apis.accounts.createAccount).to.be.a.function;

            api.apis.accounts.createAccount({
                body:{
                    login:'USER-'+Date.now(),
                    email:'foo@foo.bar',
                    timeZone:'CET',
                    language:'en',
                    name:'Generated',
                    newPassword:'password'
                }
            }).then(function(response){
                var accountCreated = response.obj;
                expect(accountCreated).to.be.an.object;
                done();
            });

        });
    });

    it('Should get current user account', function(done){

        var client = new DocdokuPlmClient({
            url:config.url,
            login:config.login,
            password:config.password,
            basicAuth:true
        });

        client.getApi().then(function(api){
            api.apis.accounts.getAccount().then(function(response){
                expect(response.obj.login).to.equal(config.login);
                done();
            });
        });
    });

});
