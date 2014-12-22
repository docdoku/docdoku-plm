module.exports = {
    verbose: true,
    failFast:true,
    logLevel: "warning",
    xunit: "results.xml",
    domain: "val.docdoku.net",
    workspace: "test",
    port: "8080",
    login: "test",
    pass: "testpassword",
    contextPath: "/",
    pre: [
        "js/pre/start.js",
    ],
    post: [
        "js/auth/logout.js"
    ],
    includes: [
        "js/includes/vars.js",
        "js/includes/helpers.js"
    ],
    paths: [
        "js/auth/login.js",
        "js/pre/clean.js",

        // Creations
        "js/product-management/part/partCreation.js",
        "js/product-management/part/partCheckin.js",
        "js/product-management/share/sharedPartCreation.js",
        "js/product-management/share/publicSharedPart.js",
        "js/product-management/share/privateSharedPart.js",
        "js/product-management/part/showPartDetails.js",
        "js/product-management/product/productCreation.js",
        "js/product-management/baseline/baselineCreation.js",
        "js/product-management/baseline/baselineDuplication.js",
        "js/product-management/product-instance/productInstanceCreation.js",
        "js/product-management/template/partTemplateCreation.js",

        "js/document-management/folder/folderCreation.js",
        "js/document-management/document/documentCreation.js",
        "js/document-management/document/documentCheckin.js",
        "js/document-management/share/sharedDocumentCreation.js",
        "js/document-management/share/publicSharedDocument.js",
        "js/document-management/share/privateSharedDocument.js",

        "js/change-management/issue/issueCreation.js",
        "js/change-management/request/requestCreation.js",
        "js/change-management/order/orderCreation.js",
        "js/change-management/milestone/milestoneCreation.js",
        "js/change-management/role/roleCreation.js",
        "js/change-management/workflow/workflowCreation.js",


        // Deletions
        "js/document-management/document/documentDeletion.js",
        "js/document-management/template/templateCreation.js",
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

    ]
};
