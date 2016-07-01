var chai = require('chai');
var expect = chai.expect;
var DocdokuPlmClient = require('../lib/docdoku-api');

var url = 'http://localhost:8080/api';

describe('Api client guest methods', function() {

    it('Should get language list', function(done) {

        var client = new DocdokuPlmClient({url:url});

        client.getApi().then(function(api){

            expect(api.apis.languages).to.be.defined;
            expect(api.apis.languages.getLanguages).to.be.a.function;

            api.apis.languages.getLanguages().then(function(response){
                expect(response.obj).to.be.an.array;
                done();
            });
        });

    });

    it('Should get timezone list', function(done) {

        var client = new DocdokuPlmClient({url:url});
        client.getApi().then(function(api){

            expect(api.apis.timezone).to.be.defined;
            expect(api.apis.timezone.getTimeZones).to.be.a.function;

            api.apis.timezone.getTimeZones().then(function(response){
                var timezone = response.obj;
                expect(timezone).to.be.an.array;
                done();
            });

        });
    });

    it('Should create an account', function(done){

        var client = new DocdokuPlmClient({url:url});
        client.getApi().then(function(api){

            expect(api.apis.accounts).to.be.defined;
            expect(api.apis.accounts.createAccount).to.be.a.function;

            api.apis.accounts.createAccount({body:{
                login:'USER-'+Date.now(),
                email:'foo@foo.bar',
                timeZone:'CET',
                language:'en',
                name:'Generated',
                newPassword:'password'
            }}).then(function(response){
                var accountCreated = response.obj
                expect(accountCreated).to.be.an.object;
                done();
            });

        });
    })


});

