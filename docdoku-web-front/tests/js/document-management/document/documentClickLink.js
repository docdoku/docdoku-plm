/*global casper,urls,workspace,documents,defaultUrl*/
casper.test.begin('Document click link tests suite', 2, function documentClickLinkTestsSuite() {
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
            this.capture('screenshot/documentClickLink/waitForFolderNavLink-error.png');
            this.test.assert(false, 'Folder nav link can not be found');
        });
    });

    /**
     * Wait for document to be displayed in list
     */

    casper.then(function waitForDocumentDisplayed() {
        return this.waitForSelector('#document-management-content table.dataTable tr[title="'+documents.document1.number+'"] td.reference', function documentIsDisplayed() {
            this.click('#document-management-content table.dataTable tr[title="'+documents.document1.number+'"] td.reference');
        }, function fail() {
            this.capture('screenshot/documentClickLink/waitForDocumentDisplayed-error.png');
            this.test.assert(false, 'Document can not be found');
        });
    });

    /**
     * Wait for document modal
     */

    casper.then(function waitForDocumentModal() {
        var modalTab = '.document-modal .tabs li a[href="#tab-iteration-links"]';

        return this.waitForSelector(modalTab, function modalOpened() {
            this.click(modalTab);
        }, function fail() {
            this.capture('screenshot/documentClickLink/waitForDocumentModal-error.png');
            this.test.assert(false, 'Document modal can not be found');
        });
    });

    /**
     * Wait for Links modal tab
     */
    casper.then(function waitForDocumentModalLinksTab() {
        return this.waitForSelector('.document-modal .linked-items-reference-typehead', function tabOpened() {
            this.test.assert(true, 'Links tab opened');
        }, function fail() {
            this.capture('screenshot/documentClickLink/waitForDocumentModalLinksTab-error.png');
            this.test.assert(false, 'Document modal Links tab can not be found');
        });
    });

    /**
     * Wait for linked document display
     */
    casper.then(function waitForLinkedDocumentDisplay() {
        return this.waitForSelector('#iteration-links > .linked-items-view > ul.linked-items > li:first-child', function linkDocumentDisplayed() {
            this.click('#iteration-links > .linked-items-view > ul.linked-items > li:first-child > a.reference');
        }, function fail() {
            this.capture('screenshot/documentClickLink/waitForLinkedDocumentDisplay-error.png');
            this.test.assert(false, 'Linked document can not be found');
        });
    });

    /**
     * Wait for linked document modal
     */
    casper.then(function waitForLinkedDocumentDisplay() {
        var modalTitle = '.document-modal > .modal-header > h3 > a[href="' + defaultUrl + '/documents/#' + workspace + '/' + documents.document1.documentLink +'/A"]';

        return this.waitForSelector(modalTitle, function linkedModalOpened() {
            this.test.assert(true, 'Linked document modal opened');
        }, function fail() {
            this.capture('screenshot/documentClickLink/waitForLinkedDocumentModal-error.png');
            this.test.assert(false, 'Linked document modal can not be found');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });

});
