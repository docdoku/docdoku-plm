var login = 'login';
var pass = 'pass';
var workspace = 'Building-modiffinal';
var authUrl = 'http://localhost:8080/';
var userInfoUrl = 'http://localhost:8080/api/workspaces/'+workspace+'/users/me';
var partCreationNumber = '000-AAA-CasperJsTest';
var partCreationName = 'CasperJsTest';
var productCreationNumber = '000-AAA-CasperJsTestProduct';
var productCreationName = 'CasperJsTestProduct';

casper.options.viewportSize = {
    width: 1680,
    height: 1050
};

casper.setFilter("page.confirm", function(msg) {
    this.echo("Confirm box: "+msg);
    return true;
});