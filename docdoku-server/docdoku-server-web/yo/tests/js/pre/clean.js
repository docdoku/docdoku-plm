/*global casper,apiUrls,helpers*/
casper.test.begin('Cleaning potential data', 0, function cleanTestsSuite() {
    'use strict';

    casper.open('');

    // Documents
    casper.then(function cleanupDocuments() {

        this.open(apiUrls.deleteDocument, {method: 'DELETE'}).then(function (response) {
            if (response.status === 200) {
                this.log('Test document has been deleted', 'info');
            } else {
                this.log('Cannot delete test document, reason : ' + helpers.findReasonInResponseHeaders(response.headers), 'warning');
            }
        });
    });

    // Folders
    casper.then(function cleanupFolders() {

        this.open(apiUrls.deleteFolder, {method: 'DELETE'}).then(function (response) {
            if (response.status === 200) {
                this.log('Test folders has been deleted', 'info');
            } else {
                this.log('Cannot delete test folders, reason : ' + helpers.findReasonInResponseHeaders(response.headers), 'warning');
            }
        });
    });

    // Products
    casper.then(function cleanupProducts() {

        this.open(apiUrls.deleteProduct, {method: 'DELETE'}).then(function (response) {
            if (response.status === 200) {
                this.log('Test products has been deleted', 'info');
            } else {
                this.log('Cannot delete test products, reason : ' + helpers.findReasonInResponseHeaders(response.headers), 'warning');
            }
        });
    });

    // Parts
    casper.then(function cleanupParts() {

        this.open(apiUrls.deletePart, {method: 'DELETE'}).then(function (response) {
            if (response.status === 200) {
                this.log('Test parts has been deleted', 'info');
            } else {
                this.log('Cannot delete test parts, reason : ' + helpers.findReasonInResponseHeaders(response.headers), 'warning');
            }
        });

    });

    casper.run(function allDone(){
        this.test.done();
    });
});