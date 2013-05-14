var login = 'test';
var pass = 'test';
var workspace = 'test';
var authUrl = 'http://val.docdoku.net:8080/';
var userInfoUrl = 'http://val.docdoku.net:8080/api/workspaces/'+workspace+'/users/me';
var partCreationNumber = '000-AAA-CasperJsTest';
var partCreationName = 'CasperJsTest';
var productCreationNumber = '000-AAA-CasperJsTestProduct';
var productCreationName = 'CasperJsTestProduct';
var folderCreationName = 'CasperJsTestFolder';

casper.options.viewportSize = {
    width: 1680,
    height: 1050
};

casper.setFilter("page.confirm", function(msg) {
    this.echo("Confirm box: "+msg);
    return true;
});