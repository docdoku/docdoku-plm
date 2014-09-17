module.exports = {
    "verbose": true,
    "log-level": "debug",
    "xunit": "results.xml",
    "domain": "val.docdoku.net",
    "workspace": "test",
    "port": "8080",
    "login": "test",
    "pass": "testpassword",
    "contextPath": "/",
    "pre": [
        "js/pre/start.js",
        "js/auth/login.js",
        "js/pre/clean.js"
    ],
    "post": [
        "js/auth/logout.js"
    ],
    "includes": [
        "js/includes/vars.js",
        "js/includes/helpers.js"
    ],
    "paths": [
        "js/product-management/part/partCreation.js",
        "js/product-management/part/showPartDetails.js",
        "js/product-management/product/productCreation.js",
        "js/product-management/product/productDeletion.js",
        "js/product-management/part/partDeletion.js",
        "js/document-management/folder/folderCreation.js",
        "js/document-management/document/documentCreation.js",
        "js/document-management/document/documentDeletion.js",
        "js/document-management/folder/folderDeletion.js",
        "js/document-management/template/templateCreation.js",
        "js/document-management/template/templateDeletion.js"
        //"js/change-management/issue/issueCreation.js"
    ]
};