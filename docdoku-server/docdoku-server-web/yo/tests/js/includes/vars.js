/*global casper*/

var domain = casper.cli.get("domain");
var port = casper.cli.get("port");
var login = casper.cli.get("login");
var pass = casper.cli.get("pass");
var workspace = casper.cli.get("workspace");
var contextPath = casper.cli.get("contextPath");

var authUrl = 'http://'+domain+':'+port + contextPath;
var productManagementUrl = authUrl+'/product-management/#'+workspace;
var documentManagementUrl = authUrl+'/document-management/#'+workspace;
var changeManagementUrl = authUrl+'/change-management/#'+workspace;
var userInfoUrl = authUrl+'/api/workspaces/'+workspace+'/users/me';

var partCreationNumber = '000-AAA-CasperJsTestPart';
var partCreationName = 'CasperJsTestPart';
var documentCreationNumber = '000-AAA-CasperJsTestDocument';
var documentTemplateCreationNumber = 'CasperJsTestDocumentTemplate';
var productCreationNumber = '000-AAA-CasperJsTestProduct';
var productCreationName = 'CasperJsTestProduct';
var folderCreationName = 'CasperJsTestFolder';
var changeIssueCreationName = 'CasperJsTestIssue';

var deletePartUrl = authUrl+'/api/workspaces/'+workspace+'/parts/'+partCreationNumber+'-A';
var deleteDocumentUrl = authUrl+'/api/workspaces/'+workspace+'/documents/'+documentCreationNumber+'-A';
var deleteProductUrl = authUrl+'/api/workspaces/'+workspace+'/products/'+productCreationNumber;
var deleteFolderUrl = authUrl+'/api/workspaces/'+workspace+'/folders/'+workspace+":"+folderCreationName;
