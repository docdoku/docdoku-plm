/*global casper*/

/*jshint -W098*/

var domain = casper.cli.get('domain');
var port = casper.cli.get('port');
var workspace = casper.cli.get('workspace');
var login = casper.cli.get('login');
var pass = casper.cli.get('pass');
var contextPath = casper.cli.get('contextPath');
var protocol = casper.cli.get('protocol');

var defaultUrl = protocol + '://' + domain + ':' + port;
var homeUrl = defaultUrl + contextPath;

function twoDigit(n) {
    'use strict';
    return n > 9 ? n : '0' + n;
}

var tomorrow = new Date(Date.now() + 86400000);
var yesterday = new Date(Date.now() - 86400000);
var tomorrowValue = tomorrow.getFullYear() + '-' + twoDigit(tomorrow.getMonth() + 1) + '-' + twoDigit(tomorrow.getDate());
var yesterdayValue = yesterday.getFullYear() + '-' + twoDigit(yesterday.getMonth() + 1) + '-' + twoDigit(yesterday.getDate());

var documents = {
    template1: {
        number: 'CasperJsTestDocumentTemplate',
        type: 'CasperJsTestDocumentTemplateType',
        mask: 'MASK-###',
        maskGenerated:'MASK-000'
    },
    folder1: 'CasperJsTestFolder',
    document1: {
        number: '000-AAA-CasperJsTestDocument',
        iterationNote: 'This is the first iteration of this document',
        sharedPassword: 'azertyuiop',
        expireDate: tomorrowValue,
        expireDate2: yesterdayValue,
        documentLink: '100-AAA-CasperJsTestDocument',
        documentLinkComment: 'Some dummy comment'
    },
    document2: {
        number: '100-AAA-CasperJsTestDocument',
        iterationNote: 'This is the first iteration of this document',
        sharedPassword: 'azertyuiop',
        expireDate: tomorrowValue,
        expireDate2: yesterdayValue
    },
    document3: {
        number: '200-AAA-CasperJsTestDocument',
        iterationNote: 'This is the first iteration of this document',
        sharedPassword: 'azertyuiop',
        expireDate: tomorrowValue,
        expireDate2: yesterdayValue
    },
    documentWithWorkflow: {
        number: '300-AAA-CasperJsTestDocument',
        iterationNote: 'This is a document with a workflow'
    },
    tags: {
        tag1: 'Foo',
        tag2: 'Bar'
    },
    lov: {
        itemName: 'casperTestItemName',
        possibleValueName: 'None',
        possibleValueValue: '0',
        color: {
            itemName: 'Color',
            namePairValueNameRed: 'Red',
            namePairValueValueRed: 'rouge',
            namePairValueNameGreen: 'Green',
            namePairValueValueGreen: 'vert',
            namePairValueNameBlue: 'Blue',
            namePairValueValueBlue: 'bleu'
        },

        template: {
            number: 'TemplateWithLOVAttributeColor',
            attributeName: 'colorAttributes'
        }

    }
};

var products = {
    template1: {
        number: 'CasperJsTestPartTemplate',
        type: 'CasperJsTestPartType',
        mask: 'FAX_***_##'
    },
    template2: {
        number: 'CasperJsTestPartTemplate2',
        type: 'CasperJsTestPartType2',
        attrInstance: 'CasperJsTestAttrInst'
    },
    part1: {
        number: '000-AAA-CasperJsTestPart',
        name: 'CasperJsTestPart',
        iterationNote: 'This is the first iteration of this part',
        sharedPassword: 'azertyuiop',
        expireDate: tomorrowValue,
        expireDate2: yesterdayValue,
        attributeValue: 'CasperJs',
        documentLink: '000-AAA-CasperJsTestDocument'
    },
    part2: {
        number: '000-AAA-CasperJsTestPart2',
        name: 'CasperJsTestPart2',
        iterationNote: 'This is the first iteration of this part',
        sharedPassword: 'azertyuiop',
        expireDate: tomorrowValue,
        expireDate2: yesterdayValue,
        attributeName1: 'CasperJsTestAttr',
        attributeName2: 'CasperJsTestAttr-lock'
    },
    product1: {
        number: '000-AAA-CasperJsTestProduct',
        description: 'CasperJsTestProduct'
    },
    assembly: {
        parts: {
            '100-AAA-CasperJsAssemblyP1': {
                tx: 100,
                ty: 150,
                tz: -140,
                rx: 1.57,
                ry: 1.57,
                rz: 0
            },
            '200-AAA-CasperJsAssemblyP2': {
                tx: -100,
                ty: -150,
                tz: -140,
                rx: -1.57,
                ry: 1.57,
                rz: 1.57
            },
            '300-AAA-CasperJsAssemblyP3': {
                tx: -100,
                ty: 150,
                tz: -140,
                rx: 1.57,
                ry: 0.95,
                rz: -1.57
            },
            '400-AAA-CasperJsAssemblyP4': {
                tx: 100,
                ty: -150,
                tz: 140,
                rx: -1.57,
                ry: 0.95,
                rz: 1.57
            }
        }
    }
};

var productInstances = {
    productInstance1: {
        serialNumber: 'CasperJsTestSerialNumber',
        iterationNote: 'First Iteration Casper',
        pathDataValue: 'CasperJsTestData'
    }
};

var baselines = {
    baseline1: {
        name: '000-AAA-CasperJsTestBaseline',
        description: 'This is a baseline'
    },
    baseline2: {
        name: '001-AAA-CasperJsTestBaseline',
        description: 'This is also a baseline'
    }
};

var changeItems = {
    changeIssue1: {
        number: 'CasperJsTestIssue'
    },
    changeRequest1: {
        number: 'CasperJsTestRequest'
    },
    changeOrder1: {
        number: 'CasperJsTestOrder'
    },
    milestone1: {
        title: 'CasperJsTestMilestone',
        date: '2015-12-12'
    }
};

var roles = {
    role1: {
        name: 'Lecteur'
    }
};

var p2pLinks = {
    type: 'FOO'
};

var workflows = {
    role1: 'CasperJsRole1',
    role2: 'CasperJsRole2',
    role3: 'CasperJsRole3',
    workflow1: {
        name: 'CasperJsTestWorkflow',
        finalState: 'CasperJsFinalState',
        activities: {
            activity1: {
                name: 'CasperJsTestActivity',
                tasks: {
                    task1: {
                        name: 'CasperJsTestTask'
                    }
                }
            }
        }
    },
    workflow2: {
        name: 'CasperJsTestWorkflow2'
    }
};

var urls = {
    productManagement: homeUrl + 'product-management/#' + workspace,
    productStructure: homeUrl + 'product-structure/#' + workspace + '/' + products.product1.number + '/config-spec/wip/bom',
    productStructureInstances: homeUrl + 'api/workspaces/' + workspace + '/products/' + products.product1.number + '/instances?configSpec=wip&path=-1',
    productStructureForDeliverable: homeUrl + 'product-structure/#' + workspace + '/' + products.product1.number + '/config-spec/pi-' + productInstances.productInstance1.serialNumber + '/bom',
    documentManagement: homeUrl + 'document-management/#' + workspace,
    changeManagement: homeUrl + 'change-management/#' + workspace,
    workspaceAdministration:homeUrl+'workspace-management/',

    documentPermalink: homeUrl + 'documents/#' + workspace + '/' + documents.document1.number + '/A',
    partPermalink: homeUrl + 'parts/#' + workspace + '/' + products.part1.number + '/A',

    // Set on share creation
    privateDocumentPermalink: null,
    privateDocumentPermalinkExpired: null,

    privatePartPermalink: null,
    privatePartPermalinkExpired: null
};

var apiUrls = {
    userInfo: homeUrl + 'api/workspaces/' + workspace + '/users/me',
    deletePart1: homeUrl + 'api/workspaces/' + workspace + '/parts/' + products.part1.number + '-A',
    deletePart2: homeUrl + 'api/workspaces/' + workspace + '/parts/' + products.part2.number + '-A',
    deletePartTemplate: homeUrl + 'api/workspaces/' + workspace + '/part-templates/' + products.template1.number,
    deletePartTemplate2: homeUrl + 'api/workspaces/' + workspace + '/part-templates/' + products.template2.number,
    deleteDocumentTemplate: homeUrl + 'api/workspaces/' + workspace + '/document-templates/' + documents.lov.template.number,
    deleteLov1: homeUrl + 'api/workspaces/' + workspace + '/lov/' + documents.lov.itemName,
    deleteLov2: homeUrl + 'api/workspaces/' + workspace + '/lov/' + documents.lov.color.itemName,
    deleteDocument1: homeUrl + 'api/workspaces/' + workspace + '/documents/' + documents.document1.number + '-A',
    deleteDocument2: homeUrl + 'api/workspaces/' + workspace + '/documents/' + documents.document2.number + '-A',
    deleteDocument3: homeUrl + 'api/workspaces/' + workspace + '/documents/' + documents.document3.number + '-A',
    deleteProduct: homeUrl + 'api/workspaces/' + workspace + '/products/' + products.product1.number,
    deleteFolder: homeUrl + 'api/workspaces/' + workspace + '/folders/' + workspace + ':' + documents.folder1,
    getBaselines: homeUrl + 'api/workspaces/' + workspace + '/products/' + products.product1.number + '/baselines',
    deleteProductInstance: homeUrl + 'api/workspaces/' + workspace + '/products/' + products.product1.number + '/product-instances/' + productInstances.productInstance1.serialNumber,
    getWorkflows: homeUrl + 'api/workspaces/' + workspace + '/workflow-models',
    getRoles: homeUrl + 'api/workspaces/' + workspace + '/roles',
    getTags: homeUrl + 'api/workspaces/' + workspace + '/tags',
    getDocuments: homeUrl + 'api/workspaces/' + workspace + '/documents',
    getParts: homeUrl + 'api/workspaces/' + workspace + '/parts',
    milestones: homeUrl + 'api/workspaces/' + workspace + '/changes/milestones',
    queries: homeUrl + 'api/workspaces/' + workspace + '/parts/queries'
};
