# Docdoku API

This module generates a JSON file that describes all web services from eplmp-server-rest module

## Build commands

    mvn clean install

## Dev commands

Validate the json file

    npm run validate
    
Launch a local instance of SwaggerUI

    npm run doc
    
Then access to `http://localhost:20000/?url=swagger.json`

## Static doc

    npm run static
    
Then access to `http://localhost:20000/?url=swagger.json`
