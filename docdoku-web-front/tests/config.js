/*global module*/
module.exports = {

    // URL
    protocol: 'http',
    domain: 'localhost',
    port: '8080',
    contextPath: '/',

    // Workspace authentication
    workspace: 'test',
    login: 'test',
    pass: 'test',

    // Configuration
    logLevel: 'warning',
    debug: false,
    verbose: true,
    failFast:true,
    xunit: 'results.xml',
    waitOnRequest: false,
    debugResponses:false,
    debugRequests:false,
    requestTimeOut: 1000, // ms
    globalTimeout: 20, // minutes
    soundOnTestsEnd:false,

    // Files to test
    pre: [
        'js/pre/start.js'
    ],
    post: [
        'js/auth/logout.js'
    ],
    includes: [
        'js/includes/vars.js',
        'js/includes/helpers.js'
    ],
    paths: [

        // Login, erase potential data
        'js/auth/login.js',
        'js/pre/clean.js',

        // Content Type check
        'js/content-type/contentTypeCheck.js',

        // Workflow creation
        'js/change-management/role/roleCreation.js',
        'js/change-management/workflow/workflowCreation.js',
        'js/change-management/workflow/workflowDuplication.js',

        // Documents tags
        'js/document-management/tag/tagCreation.js',
        'js/document-management/tag/tagList.js',

        // Document templates
        'js/document-management/template/templateCreation.js',

        // Folder and document creation
        'js/document-management/folder/folderCreation.js',
        'js/document-management/document/documentCreationFromTemplate.js',
        'js/document-management/document/documentCreationWithWorkflow.js',
        'js/document-management/document/documentCreation.js',
        'js/document-management/document/documentsCreation.js',
        'js/document-management/document/documentUploadFile.js',
        'js/document-management/document/documentFilesRemove.js',
        'js/document-management/document/documentMultipleCheckin.js',
        'js/document-management/document/documentMultipleCheckout.js',
        'js/document-management/document/documentAddLink.js',
        'js/document-management/document/documentClickLink.js',
        'js/document-management/document/documentMultipleUndoCheckout.js',
        'js/document-management/document/documentCheckout.js',
        'js/document-management/document/documentCheckin.js',

        // Document sharing
        'js/document-management/share/sharedDocumentCreation.js',
        'js/document-management/share/publicSharedDocument.js',
        'js/document-management/share/privateSharedDocument.js',
        'js/document-management/share/expiredSharedDocument.js',


        // Part templates
        'js/product-management/template/partTemplateCreation.js',
        'js/product-management/template/templateWithAttribute.js',

        // Part and assembly creation
        'js/product-management/part/partCreation.js',
        'js/product-management/part/showPartDetails.js',
        'js/product-management/part/partUploadNativeCadFile.js',
        'js/product-management/part/partAddLink.js',
        'js/product-management/part/partClickLink.js',
        'js/product-management/part/partCheckin.js',
        'js/product-management/part/partCheckout.js',
        'js/product-management/assembly/assemblyCreation.js',
        'js/product-management/assembly/assemblyCheck.js',
        'js/product-management/part/partCheckin.js',
        'js/product-management/part/partsMultipleCheckout.js',
        'js/product-management/part/partsMultipleCheckin.js',
        'js/product-management/part/partsMultipleCheckout.js',
        'js/product-management/part/partsMultipleUndoCheckout.js',
        'js/product-management/part/partRelease.js',
        'js/product-management/part/partObsolete.js',

        // Part sharing
        'js/product-management/share/sharedPartCreation.js',
        'js/product-management/share/publicSharedPart.js',
        'js/product-management/share/expiredSharedPart.js',
        'js/product-management/share/privateSharedPart.js',

        // Product and baseline creation
        'js/product-management/product/productCreation.js',
        'js/product-management/pathToPathLink/pathToPathLinkCreation.js',
        'js/product-management/baseline/baselineCreation.js',
        'js/product-management/product-instance/productInstanceCreation.js',

        // Product structure
        'js/product-management/assembly/bomInspection.js',
        'js/product-management/assembly/instancesCheck.js',
        'js/product-management/product-instance/productInstanceData.js',
        'js/product-management/pathToPathLink/pathToPathLinkCheck.js',
        'js/product-management/part/checkUsedByList.js',

        // Change items creation
        'js/change-management/issue/issueCreation.js',
        'js/change-management/request/requestCreation.js',
        'js/change-management/order/orderCreation.js',
        'js/change-management/milestone/milestoneCreation.js',

        //LOV Creation
        'js/document-management/lov/lovCreation.js',

        // Attributes creation
        'js/common/attributes.js',
        'js/common/partFromTemplate.js',

        // Query builder
        "js/product-management/queryBuilder/queryBuilderSearch.js",

        // Deletions
        'js/product-management/product-instance/productInstanceDeletion.js',
        'js/product-management/baseline/baselineDeletion.js',
        'js/product-management/product/productDeletion.js',
        'js/product-management/part/partDeletion.js',
        'js/product-management/part/partMultipleDeletion.js',
        'js/product-management/template/partTemplateDeletion.js',
        'js/document-management/lov/lovDeletion.js',

        'js/document-management/tag/tagDeletion.js',
        'js/document-management/document/documentDeletion.js',
        'js/document-management/document/documentMultipleDeletion.js',
        'js/document-management/template/templateDeletion.js',
        'js/document-management/folder/folderDeletion.js',

        'js/change-management/workflow/workflowDeletion.js',
        'js/change-management/issue/issueDeletion.js',
        'js/change-management/milestone/milestoneDeletion.js',
        'js/change-management/order/orderDeletion.js',
        'js/change-management/request/requestDeletion.js',

        //Create a document template with a LOV attribute, needs an empty list of documents template, and an empty list of LOV
        'js/document-management/lov/lovInTemplateCreation.js'

    ]
};
