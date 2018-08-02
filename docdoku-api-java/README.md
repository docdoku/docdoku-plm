# Docdoku API Java

Generates a jar that could be used in other projects to consume DocDokuPLM web services

## Documentation

Create a client, then use http services, example :

    ApiClient client = new DocDokuPLMBasicClient("http://localhost:8080/api", login, password).getClient();
    AccountDTO myAccount = new AccountsApi(client).getAccount();
    
See all classes under `src/test/java` for api usage

## Development guide

Build 
    
* Run `mvn clean install` docdoku-api module
* Run `mvn clean install` this module

Tests

Tests needs a running server and are skipped from build, they validate the build from generated sources. Edit TestConfig.java if needed.
