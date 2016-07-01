// Use this lib as a node module

var DocdokuPlmClient = require('../lib/docdoku-api');

var client = new DocdokuPlmClient({url:'http://localhost:8080/api'});

client.getApi().then(function(api){
    api.languages.getLanguages().then(function(response){
      console.log(response.obj);
    });
});

