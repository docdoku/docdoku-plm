/*global casper*/

var domain = casper.cli.get("domain");
var port = casper.cli.get("port");
var login = casper.cli.get("login");
var pass = casper.cli.get("pass");
var workspace = casper.cli.get("workspace");
var contextPath = casper.cli.get("contextPath");

var homeUrl = 'http://'+domain+':'+port + contextPath;
var productManagementUrl = homeUrl+'/product-management/#'+workspace;
var documentManagementUrl = homeUrl+'/document-management/#'+workspace;
var changeManagementUrl = homeUrl+'/change-management/#'+workspace;
var userInfoUrl = homeUrl+'/api/workspaces/'+workspace+'/users/me';

var partCreationNumber = '000-AAA-CasperJsTestPart';
var partCreationName = 'CasperJsTestPart';
var documentCreationNumber = '000-AAA-CasperJsTestDocument';
var documentTemplateCreationNumber = 'CasperJsTestDocumentTemplate';
var productCreationNumber = '000-AAA-CasperJsTestProduct';
var productCreationName = 'CasperJsTestProduct';
var folderCreationName = 'CasperJsTestFolder';
var changeIssueCreationName = 'CasperJsTestIssue';

var deletePartUrl = homeUrl+'/api/workspaces/'+workspace+'/parts/'+partCreationNumber+'-A';
var deleteDocumentUrl = homeUrl+'/api/workspaces/'+workspace+'/documents/'+documentCreationNumber+'-A';
var deleteProductUrl = homeUrl+'/api/workspaces/'+workspace+'/products/'+productCreationNumber;
var deleteFolderUrl = homeUrl+'/api/workspaces/'+workspace+'/folders/'+workspace+":"+folderCreationName;
