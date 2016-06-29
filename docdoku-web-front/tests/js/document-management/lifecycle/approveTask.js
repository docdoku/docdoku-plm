/*global casper,urls,workspace,documents*/

casper.test.begin('Document approve task tests suite', 7, function documentApproveTaskTestsSuite() {

    'use strict';

    casper.open('');

    /**
     * Open document management URL
     * */

    casper.then(function () {
        return this.open(urls.documentManagement);
    });

    /**
     * Open folder nav
     */

    casper.then(function waitForFolderNavLink() {
        return this.waitForSelector('a[href="#' + workspace + '/folders/' + documents.folder1 + '"]', function () {
            this.click('a[href="#' + workspace + '/folders/' + documents.folder1 + '"]');
        }, function fail() {
            this.capture('screenshot/approveTask/waitForFolderNavLink-error.png');
            this.test.assert(false, 'Folder nav link can not be found');
        });
    });

    /***
     * Open the document modal
     */
    casper.then(function openCreatedDocument() {
        this.click('#document-management-content table.dataTable tr[title="' + documents.documentWithWorkflow.number + '"] td.reference');
        var modalTab = '.document-modal .tabs li a[href="#tab-iteration-files"]';
        return this.waitForSelector(modalTab, function modalOpened() {
            this.click(modalTab);
        }, function fail() {
            this.capture('screenshot/approveTask/openCreatedDocument-error.png');
            this.test.assert(false, 'Document modal can not be found');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });

});
