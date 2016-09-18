var chai = require('chai');
var expect = chai.expect;
var DocdokuPlmClient = require('../lib/docdoku-api');
var config = require('./test.config');

describe('Timezones API tests', function() {

    it('Should get timezone list', function(done) {

        var client = new DocdokuPlmClient({url:config.url});
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



});

