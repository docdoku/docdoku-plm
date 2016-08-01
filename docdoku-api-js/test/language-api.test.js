var chai = require('chai');
var expect = chai.expect;
var DocdokuPlmClient = require('../lib/docdoku-api');
var config = require('./test.config');

describe('Languages API tests', function() {

    it('Should get language list', function(done) {

        var client = new DocdokuPlmClient({url:config.url});

        client.getApi().then(function(api){

            expect(api.apis.languages).to.be.defined;
            expect(api.apis.languages.getLanguages).to.be.a.function;

            api.apis.languages.getLanguages().then(function(response){
                expect(response.obj).to.be.an.array;
                done();
            });
        });

    });

});

