var domain = 'val.docdoku.net';
var port = '8080';
var login = 'test';
var pass = 'test';
var workspace = 'test';

var authUrl = 'http://'+domain+':'+port+'/';
var userInfoUrl = authUrl+'/api/workspaces/'+workspace+'/users/me';

var partCreationNumber = '000-AAA-CasperJsTestPart';
var partCreationName = 'CasperJsTestPart';
var documentCreationNumber = '000-AAA-CasperJsTestDocument';
var documentCreationName = 'CasperJsTestDocument';
var productCreationNumber = '000-AAA-CasperJsTestProduct';
var productCreationName = 'CasperJsTestProduct';
var folderCreationName = 'CasperJsTestFolder';

var deletePartUrl = authUrl+'/api/workspaces/'+workspace+'/parts/'+partCreationNumber+'-A';
var deleteDocumentUrl = authUrl+'/api/workspaces/'+workspace+'/documents/'+documentCreationNumber+'-A';
var deleteProductUrl = authUrl+'/api/workspaces/'+workspace+'/products/'+productCreationNumber;
var deleteFolderUrl = authUrl+'/api/workspaces/'+workspace+'/folders/'+folderCreationName;

casper.options.viewportSize = {
    width: 1680,
    height: 1050
};

casper.setFilter("page.confirm", function(msg) {
    this.echo("Confirm box: "+msg);
    return true;
});