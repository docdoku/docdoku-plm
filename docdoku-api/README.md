# Docdoku API

This module generate the REST API Reference sites 

## Build commands

**Generate a static html page && the swagger-ui-dist**

    mvn clean install
    
Build properties (defaults)

    -Denv.API_HOST=https://docdokuplm.net
    -Denv.API_BASE_PATH=/eplmp-server-rest/api
    -Denv.API_TITLE=DocDokuPLM API
    -Denv.API_VERSION=${project.version}
    -Denv.API_DESCRIPTION=DocDokuPLM API Description and Reference
    -Denv.API_TOS_URL=https://www.docdokuplm.com/
    -Denv.API_CONTACT_MAIL=contact@docdokuplm.com
    -Denv.API_CONTACT_NAME=Docdoku
    -Denv.API_CONTACT_URL=https://www.docdokuplm.com/
    -Denv.API_LICENCE_URL=GNU AFFERO GENERAL PUBLIC LICENSE Version 3
    -Denv.API_LICENCE_NAME=AGPL-3.0
    
## Dev commands

Validate the json file

    npm run validate
    
**Swagger ui** 
  
Launch a local instance of SwaggerUI

    npm run doc
    
Then access to `http://localhost:20000`

**Static doc**

    npm run static
    
Then access to `http://localhost:9010`
