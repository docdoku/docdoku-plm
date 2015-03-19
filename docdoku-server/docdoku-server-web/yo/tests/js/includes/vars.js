/*global casper*/

var domain = casper.cli.get("domain");
var port = casper.cli.get("port");
var login = casper.cli.get("login");
var pass = casper.cli.get("pass");
var workspace = casper.cli.get("workspace");
var contextPath = casper.cli.get("contextPath");
var protocol = casper.cli.get("protocol");

var homeUrl = protocol + '://' + domain + ':' + port + contextPath;

function twoDigit(n){
    'use strict';
    return n > 9 ? n : "0" + n;
}

var tomorrow = new Date(Date.now()+86400000);
var yesterday = new Date(Date.now()-86400000);
var tomorrowValue = tomorrow.getFullYear()+'-'+twoDigit(tomorrow.getMonth()+1)+'-'+twoDigit(tomorrow.getDate());
var yesterdayValue = yesterday.getFullYear()+'-'+twoDigit(yesterday.getMonth()+1)+'-'+twoDigit(yesterday.getDate());

var documents = {
	template1 : {
		number : 'CasperJsTestDocumentTemplate'
	},
	folder1: 'CasperJsTestFolder',
	document1 : {
		number : '000-AAA-CasperJsTestDocument',
        iterationNote:'This is the first iteration of this document',
        sharedPassword:'azertyuiop',
        expireDate:tomorrowValue,
        expireDate2:yesterdayValue
	},
    document2 : {
        number : '100-AAA-CasperJsTestDocument',
        iterationNote:'This is the first iteration of this document',
        sharedPassword:'azertyuiop',
        expireDate:tomorrowValue,
        expireDate2:yesterdayValue
    },
    document3 : {
        number : '200-AAA-CasperJsTestDocument',
        iterationNote:'This is the first iteration of this document',
        sharedPassword:'azertyuiop',
        expireDate:tomorrowValue,
        expireDate2:yesterdayValue
    },
    tags:{
        tag1:'Foo',
        tag2:'Bar'
    },
    lov : {
        itemName : 'casperTestItemName',
        possibleValueName : 'None',
        possibleValueValue : '0'
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
        iterationNote:'This is the first iteration of this part',
        sharedPassword:'azertyuiop',
        expireDate:tomorrowValue,
        expireDate2:yesterdayValue
	},
	product1: {
		number : '000-AAA-CasperJsTestProduct',
		name : 'CasperJsTestProduct'
	},
    assembly:{
        parts: {
            '100-AAA-CasperJsAssemblyP1':{
                tx:100,ty:150,tz:-140,
                rx:1.57,ry:1.57,rz:0
            },
            '200-AAA-CasperJsAssemblyP2':{
                tx:-100,ty:-150,tz:-140,
                rx:-1.57,ry:1.57,rz:1.57
            },
            '300-AAA-CasperJsAssemblyP3':{
                tx:-100,ty:150,tz:-140,
                rx:1.57,ry:0.95,rz:-1.57
            },
            '400-AAA-CasperJsAssemblyP4':{
                tx:100,ty:-150,tz:140,
                rx:-1.57,ry:0.95,rz:1.57
            }
        }
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
    productStructure : homeUrl+'product-structure/#'+workspace+'/' + products.product1.number,
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
    deleteLov : homeUrl+'api/workspaces/'+workspace+'/lov/'+documents.lov.itemName,
	deleteDocument : homeUrl+'api/workspaces/'+workspace+'/documents/'+documents.document1.number+'-A',
	deleteProduct : homeUrl+'api/workspaces/'+workspace+'/products/'+products.product1.number,
	deleteFolder : homeUrl+'api/workspaces/'+workspace+'/folders/'+workspace+":"+documents.folder1,
	getBaselines : homeUrl+'api/workspaces/'+workspace+'/products/'+products.product1.number+'/baselines',
    deleteProductInstance : homeUrl+'api/workspaces/'+workspace+'/products/'+products.product1.number+'/product-instances/'+productInstances.productInstance1.serialNumber,
    getWorkflows : homeUrl+'api/workspaces/'+workspace+'/workflows',
    getRoles : homeUrl+'api/workspaces/'+workspace+'/roles',
    getTags : homeUrl+'api/workspaces/'+workspace+'/tags'
};
