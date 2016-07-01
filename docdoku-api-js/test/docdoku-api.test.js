var chai = require('chai');
var expect = chai.expect;
var DocdokuPlmClient = require('../lib/docdoku-api');

var url = 'http://localhost:8080/api';

describe('Api client creation', function() {
    it('Should affect options to client when using constructor', function() {
        var client = new DocdokuPlmClient({url:url});
        expect(client.options.url).to.be.defined;
        expect(client.options.url).to.equal(url);
    });

    it('Should modify options with setter', function() {
        var client = new DocdokuPlmClient();
        client.setOptions({url:url})
        expect(client.options.url).to.be.defined;
        expect(client.options.url).to.equal(url);
    });

    it('Should load the spec and give an api object', function() {
        var client = new DocdokuPlmClient({url:url});
        client.getApi().then(function(api){
            expect(api).to.be.defined;
            expect(api).to.be.an.object;
        });
    });

});
