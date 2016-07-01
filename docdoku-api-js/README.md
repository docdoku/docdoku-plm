# Docdoku API JS

Generate a node module to integrate in your nodejs and/or browser application

## Documentation

This module is designed to work in a nodejs or a browser application

### Browser

Add the script to your page, then it's available from the window object

    <script src="docdoku-api.js"></script>
    <script>
        var client = new DocdokuPlmClient({url: 'http://localhost:8080/api'});

        client.getApi().then(function (api) {
            api.languages.getLanguages().then(function (response) {
                var languages = response.obj;
                languages.forEach(function (lang) {
                    console.log('Available ' + lang);
                });
            });
        });
    </script>


### Node Js

Install package, then in your app

    var DocdokuPlmClient = require('../lib/docdoku-api');
    
    var client = new DocdokuPlmClient({url:'http://localhost:8080/api'});
    
    client.getApi().then(function(api){
        api.languages.getLanguages().then(function(response){
          console.log(response.obj);
        });
    });


## Development guide

Package folders description

* `lib` : main module sources
* `test` : test sources. Uses mocha (command : `mocha run test`)

Build 
    
* Run `mvn clean install` in docdoku-api module
* Run `npm run build` in this module directory

Tests

* Run `npm install && grunt test`. Some tests need a running server.
* Browser testing : run `npm run build`, then launch a static server in dist folder `http-server -p 8086` to see results in browser
