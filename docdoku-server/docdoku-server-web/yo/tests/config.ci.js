/*global module*/
module.exports = {
    verbose: true,
    failFast:true,
    logLevel: "warning",
    xunit: "results.xml",
    protocol: "http",
    domain: "val.docdoku.net",
    workspace: "test",
    port: "8080",
    login: "test",
    pass: "testpassword",
    contextPath: "/",
    pre: [
        "js/pre/start.js"
    ],
    post: [
        "js/auth/logout.js"
    ],
    includes: [
        "js/includes/vars.js",
        "js/includes/helpers.js"
    ],
    paths: [

        // Login, erase potential data
        "js/auth/login.js",
        "js/pre/clean.js",

        // Workflow creation
        "js/change-management/role/roleCreation.js",
        "js/change-management/workflow/workflowCreation.js",

        // Part and assembly creation
        "js/product-management/part/partCreation.js",
        "js/product-management/part/showPartDetails.js",
        "js/product-management/part/partUploadNativeCadFile.js",
        "js/product-management/part/partCheckin.js",
        "js/product-management/part/partCheckout.js",
        "js/product-management/assembly/assemblyCreation.js",
        "js/product-management/assembly/assemblyCheck.js",
        "js/product-management/part/partCheckin.js",
        "js/product-management/part/partsMultipleCheckout.js",
        "js/product-management/part/partsMultipleCheckin.js",
        "js/product-management/part/partsMultipleCheckout.js",
        "js/product-management/part/partsMultipleUndoCheckout.js",

        // Part sharing
        "js/product-management/share/sharedPartCreation.js",
        "js/product-management/share/publicSharedPart.js",
        "js/product-management/share/expiredSharedPart.js",
        "js/product-management/share/privateSharedPart.js",

        // Product and baseline creation
        "js/product-management/product/productCreation.js",
        "js/product-management/baseline/baselineCreation.js",
        "js/product-management/product-instance/productInstanceCreation.js",
        "js/product-management/template/partTemplateCreation.js",

        // Product structure
        "js/product-management/assembly/bomInspection.js",


        // Documents tags
        "js/document-management/tag/tagCreation.js",
        "js/document-management/tag/tagList.js",

        // Document templates
        "js/document-management/template/templateCreation.js",

        // Folder and document creation
        "js/document-management/folder/folderCreation.js",
        "js/document-management/document/documentCreation.js",
        "js/document-management/document/documentUploadFile.js",
        "js/document-management/document/documentCheckin.js",

        // Document sharing
        "js/document-management/share/sharedDocumentCreation.js",
        "js/document-management/share/publicSharedDocument.js",
        "js/document-management/share/privateSharedDocument.js",
        "js/document-management/share/expiredSharedDocument.js",

        // Change items creation
        "js/change-management/issue/issueCreation.js",
        "js/change-management/request/requestCreation.js",
        "js/change-management/order/orderCreation.js",
        "js/change-management/milestone/milestoneCreation.js",

        //LOV Creation
        "js/document-management/lov/lovCreation.js",

        // Deletions
        "js/document-management/tag/tagDeletion.js",
        "js/document-management/document/documentDeletion.js",
        "js/document-management/template/templateDeletion.js",
        "js/document-management/folder/folderDeletion.js",

        "js/change-management/workflow/workflowDeletion.js",
        "js/change-management/issue/issueDeletion.js",
        "js/change-management/milestone/milestoneDeletion.js",
        "js/change-management/order/orderDeletion.js",
        "js/change-management/request/requestDeletion.js",

        "js/product-management/product-instance/productInstanceDeletion.js",
        "js/product-management/baseline/baselineDeletion.js",
        "js/product-management/product/productDeletion.js",
        "js/product-management/part/partDeletion.js",
        "js/product-management/template/partTemplateDeletion.js",
        "js/document-management/lov/lovDeletion.js",

        //Create a document template with a LOV attribut, needs an empty list of documents template, and an empty list of LOV
        "js/document-management/lov/lovInTemplateCreation.js"


    ]
};
