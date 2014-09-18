/*global casper*/

casper.test.begin('Cleaning potential data', 0, function cleanTestsSuite() {

    'use strict';

    casper.open('');

    // Documents
    casper.then(function cleanupDocuments() {

        this.open(deleteDocumentUrl, {method: 'DELETE'}).then(function (response) {
            if (response.status === 200) {
                this.log('Test document has been deleted', 'info');
            } else {
                this.log('Cannot delete test document, reason : ' + findReasonInResponseHeaders(response.headers), 'warning');
            }
        });
    });

    // Folders
    casper.then(function cleanupFolders() {

        this.open(deleteFolderUrl, {method: 'DELETE'}).then(function (response) {
            if (response.status === 200) {
                this.log('Test folders has been deleted', 'info');
            } else {
                this.log('Cannot delete test folders, reason : ' + findReasonInResponseHeaders(response.headers), 'warning');
            }
        });
    });

    // Products
    casper.then(function cleanupProducts() {

        this.open(deleteProductUrl, {method: 'DELETE'}).then(function (response) {
            if (response.status === 200) {
                this.log('Test products has been deleted', 'info');
            } else {
                this.log('Cannot delete test products, reason : ' + findReasonInResponseHeaders(response.headers), 'warning');
            }
        });
    });

    // Parts
    casper.then(function cleanupParts() {

        this.open(deletePartUrl, {method: 'DELETE'}).then(function (response) {
            if (response.status === 200) {
                this.log('Test parts has been deleted', 'info');
            } else {
                this.log('Cannot delete test parts, reason : ' + findReasonInResponseHeaders(response.headers), 'warning');
            }
        });

    });

    casper.run(function allDone(){
        this.test.done();
    });
});
