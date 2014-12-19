/*global casper*/

var domain = casper.cli.get("domain");
var port = casper.cli.get("port");
var login = casper.cli.get("login");
var pass = casper.cli.get("pass");
var workspace = casper.cli.get("workspace");
var contextPath = casper.cli.get("contextPath");

var homeUrl = 'http://'+domain+':'+port + contextPath;

var documents = {
	template1 : {
		number : 'CasperJsTestDocumentTemplate'
	},
	folder1: 'CasperJsTestFolder',
	document1 : {
		number : '000-AAA-CasperJsTestDocument'
	}
};

var products = {
	template1 : {
		number : 'CasperJsTestPartTemplate'
	},
	part1 : {
		number : '000-AAA-CasperJsTestPart',
		name : 'CasperJsTestPart',
        iterationNode:'This is the first iteration'
	},
	product1: {
		number : '000-AAA-CasperJsTestProduct',
		name : 'CasperJsTestProduct'
	}
};

var baselines = {
    baseline1:{
        name: '000-AAA-CasperJsTestBaseline',
        description: 'This is a baseline'
    },
    baseline2:{
        name: '001-AAA-CasperJsTestBaseline',
        description: 'This is also a baseline'
    }
};

var workflows = {
	role1 : 'CasperJsRole1',
	role2 : 'CasperJsRole2',
	role3 : 'CasperJsRole3'
};

var changeItems = {
	changeIssue1 : {
		number : 'CasperJsTestIssue'
	},
    changeRequest1:{
        number: 'CasperJsTestRequest'
    },
    changeOrder1:{
        number: 'CasperJsTestOrder'
    },
    milestone1:{
        title:'CasperJsTestMilestone',
        date:'31/12/2015'
    }
};

var roles = {
    role1:{
        name:'Lecteur'
    }
};

var urls = {
	productManagement : homeUrl+'/product-management/#'+workspace,
	documentManagement : homeUrl+'/document-management/#'+workspace,
	changeManagement : homeUrl+'/change-management/#'+workspace
};

var apiUrls = {
	userInfo : homeUrl+'/api/workspaces/'+workspace+'/users/me',
	deletePart : homeUrl+'/api/workspaces/'+workspace+'/parts/'+products.part1.number+'-A',
	deleteDocument : homeUrl+'/api/workspaces/'+workspace+'/documents/'+documents.document1.number+'-A',
	deleteProduct : homeUrl+'/api/workspaces/'+workspace+'/products/'+products.product1.number,
	deleteFolder : homeUrl+'/api/workspaces/'+workspace+'/folders/'+workspace+":"+documents.folder1
};
