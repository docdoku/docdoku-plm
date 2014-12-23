/*global casper*/

var domain = casper.cli.get("domain");
var port = casper.cli.get("port");
var login = casper.cli.get("login");
var pass = casper.cli.get("pass");
var workspace = casper.cli.get("workspace");
var contextPath = casper.cli.get("contextPath");

var homeUrl = 'http://'+domain+':'+port + contextPath;

var tomorrow = new Date(Date.now()+86400000);
var yesterday = new Date(Date.now()-86400000);
var tomorrowValue = tomorrow.getFullYear()+'-'+(tomorrow.getMonth()+1)+'-'+tomorrow.getDate();
var yesterdayValue = yesterday.getFullYear()+'-'+(yesterday.getMonth()+1)+'-'+yesterday.getDate();

var documents = {
	template1 : {
		number : 'CasperJsTestDocumentTemplate'
	},
	folder1: 'CasperJsTestFolder',
	document1 : {
		number : '000-AAA-CasperJsTestDocument',
        iterationNote:'This is the first iteration',
        sharedPassword:'azertyuiop',
        expireDate:tomorrowValue,
        expireDate2:yesterdayValue
	},
    tags:{
        tag1:'Foo',
        tag2:'Bar'
    }
};

var products = {
	template1 : {
        number : 'CasperJsTestPartTemplate',
        type : 'CasperJsTestPartType',
        mask:'FAX_***_##'
	},
	part1 : {
		number : '000-AAA-CasperJsTestPart',
		name : 'CasperJsTestPart',
        iterationNote:'This is the first iteration',
        sharedPassword:'azertyuiop',
        expireDate:tomorrowValue,
        expireDate2:yesterdayValue
	},
	product1: {
		number : '000-AAA-CasperJsTestProduct',
		name : 'CasperJsTestProduct'
	}
};

var productInstances = {
    productInstance1 : {
        serialNumber:'CasperJsTestSerialNumber'
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

var workflows = {
    role1 : 'CasperJsRole1',
    role2 : 'CasperJsRole2',
    role3 : 'CasperJsRole3',
    workflow1:{
        name:'CasperJsTestWorkflow',
        finalState:'CasperJsFinalState',
        activities:{
            activity1 : {
                name:'CasperJsTestActivity',
                tasks:{
                    task1:{
                        name:'CasperJsTestTask'
                    }
                }
            }
        }
    }
};

var urls = {
	productManagement : homeUrl+'product-management/#'+workspace,
	documentManagement : homeUrl+'document-management/#'+workspace,
	changeManagement : homeUrl+'change-management/#'+workspace,
    documentPermalink:homeUrl+'documents/'+workspace+'/'+documents.document1.number+'/A',
    partPermalink:homeUrl+'parts/'+workspace+'/'+products.part1.number+'/A',

    // Set on share creation
    privateDocumentPermalink:null,
    privateDocumentPermalinkExpired:null,

    privatePartPermalink:null,
    privatePartPermalinkExpired:null
};

var apiUrls = {
	userInfo : homeUrl+'api/workspaces/'+workspace+'/users/me',
	deletePart : homeUrl+'api/workspaces/'+workspace+'/parts/'+products.part1.number+'-A',
    deletePartTemplate : homeUrl+'api/workspaces/'+workspace+'/part-templates/'+products.template1.number,
	deleteDocument : homeUrl+'api/workspaces/'+workspace+'/documents/'+documents.document1.number+'-A',
	deleteProduct : homeUrl+'api/workspaces/'+workspace+'/products/'+products.product1.number,
	deleteFolder : homeUrl+'api/workspaces/'+workspace+'/folders/'+workspace+":"+documents.folder1,
	getBaselines : homeUrl+'api/workspaces/'+workspace+'/products/'+products.product1.number+'/baselines',
    deleteProductInstance : homeUrl+'api/workspaces/'+workspace+'/products/'+products.product1.number+'/product-instances/'+productInstances.productInstance1.serialNumber
};
