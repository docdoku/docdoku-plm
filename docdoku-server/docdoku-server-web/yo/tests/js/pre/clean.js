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

    // Product instances
    casper.then(function cleanupProductInstances() {
        this.open(apiUrls.deleteProductInstance, {method: 'DELETE'}).then(function (response) {
            if (response.status === 200) {
                this.log('Product instance has been deleted', 'info');
            } else {
                this.log('Cannot delete product instance, status '+ response.status +',reason : ' + helpers.findReasonInResponseHeaders(response.headers), 'warning');
            }
        });
    });


    // Baselines
    casper.then(function cleanupBaselines() {

        var that = this;

        this.open(apiUrls.getBaselines, {method: 'GET'}).then(function (response) {
            if (response.status === 200) {
                var baselines = JSON.parse(this.getPageContent());
                baselines.forEach(function(baseline) {
                    that.log('Deleting baseline '+baseline.id,'info');
                    that.open(apiUrls.getBaselines+'/'+baseline.id,{method: 'DELETE'}).then(function(){
                        that.log('Baseline '+baseline.id+' deleted','info');
                    });
                });

            } else {
                this.log('Cannot get baselines for product, reason : ' + helpers.findReasonInResponseHeaders(response.headers), 'warning');
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

    // Part templates
    casper.then(function cleanupPartTemplates() {

        this.open(apiUrls.deletePartTemplate, {method: 'DELETE'}).then(function (response) {
            if (response.status === 200) {
                this.log('Test part templates has been deleted', 'info');
            } else {
                this.log('Cannot delete test part templates, reason : ' + helpers.findReasonInResponseHeaders(response.headers), 'warning');
            }
        });

    });

    casper.run(function allDone(){
        this.test.done();
    });
});
