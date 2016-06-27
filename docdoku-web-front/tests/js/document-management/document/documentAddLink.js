/*global casper,urls,workspace,documents*/
casper.test.begin('Document add link tests suite', 3, function documentAddLinkTestsSuite() {
    'use strict';

    casper.open('');

    /**
     * Open document management URL
     * */

    casper.then(function () {
        this.open(urls.documentManagement);
    });

    /**
     * Open folder nav
     */

    casper.then(function waitForFolderNavLink() {
        this.waitForSelector('a[href="#' + workspace + '/folders/' + documents.folder1 + '"]', function () {
            this.click('a[href="#' + workspace + '/folders/' + documents.folder1 + '"]');
        }, function fail() {
            this.capture('screenshot/documentAddLink/waitForFolderNavLink-error.png');
            this.test.assert(false, 'Folder nav link can not be found');
        });
    });

    /**
     * Wait for document to be displayed in list
     */

    casper.then(function waitForDocumentDisplayed() {
        this.waitForSelector('#document-management-content table.dataTable tr[title="'+documents.document1.number+'"] td.reference', function documentIsDisplayed() {
            this.click('#document-management-content table.dataTable tr[title="'+documents.document1.number+'"] td.reference');
        }, function fail() {
            this.capture('screenshot/documentAddLink/waitForDocumentDisplayed-error.png');
            this.test.assert(false, 'Document can not be found');
        });
    });

    /**
     * Wait for document modal
     */

    casper.then(function waitForDocumentModal() {
        var modalTab = '.document-modal .tabs li a[href="#tab-iteration-links"]';

        this.waitForSelector(modalTab, function modalOpened() {
            this.click(modalTab);
        }, function fail() {
            this.capture('screenshot/documentAddLink/waitForDocumentModal-error.png');
            this.test.assert(false, 'Document modal can not be found');
        });
    });

    /**
     * Wait for Links modal tab
     */
    casper.then(function waitForDocumentModalLinksTab() {
        this.waitForSelector('.document-modal .linked-items-reference-typehead', function tabOpened() {
            this.test.assert(true, 'Links tab opened');
        }, function fail() {
            this.capture('screenshot/documentAddLink/waitForDocumentModalLinksTab-error.png');
            this.test.assert(false, 'Document modal Links tab can not be found');
        });
    });

    /**
     * Wait for documents select list
     */
    casper.then(function waitForDocumentsSelectList() {
        this.sendKeys('.document-modal .linked-items-reference-typehead', documents.document1.documentLink, {reset: true});

        this.waitForSelector('#iteration-links > div > ul.typeahead.dropdown-menu > li:first-child > a', function documentsSelectListDisplayed() {
            this.click('#iteration-links > div > ul.typeahead.dropdown-menu > li:first-child > a');
        }, function fail() {
            this.capture('screenshot/documentAddLink/waitForDocumentsSelectList-error.png');
            this.test.assert(false, 'Documents select list can not be found');
        });
    });

    /**
     * Wait for linked document display
     */
    casper.then(function waitForLinkedDocumentDisplay() {
        this.waitForSelector('.document-modal .linked-items-view > ul.linked-items > li:first-child > a.reference', function linkDocumentDisplayed() {
            this.test.assert(true, 'Link added');
        }, function fail() {
            this.capture('screenshot/documentAddLink/waitForLinkedDocumentDisplay-error.png');
            this.test.assert(false, 'Linked document can not be found and saved');
        });
    });

    /**
     * Set a comment
     */

    casper.then(function openLinkComment(){
        var descriptionButton = '.document-modal .linked-items-view > ul.linked-items > li:first-child > a.edit-linked-item-comment';
        var descriptionInput = '.document-modal .linked-items-view > ul.linked-items > li:first-child .commentInput';
        this.click(descriptionButton);
        this.waitForSelector(descriptionInput,function openDescription(){
            this.sendKeys(descriptionInput,documents.document1.documentLinkComment,{reset:true});
            this.click('.document-modal .linked-items-view > ul.linked-items > li:first-child .validate-comment');
        },function fail(){
            this.capture('screenshot/documentAddLink/addLinkComment-error.png');
            this.test.assert(false, 'Cannot edit document link comment');
        });
    });

    /**
     * Save and wait for the modal to be closed
     */

    casper.then(function savePartIteration(){
        this.click('#save-iteration');
        this.waitWhileSelector('.document-modal', function modalClosed() {
            this.test.assert(true, 'Document modal has been closed');
        }, function fail() {
            this.capture('screenshot/documentAddLink/waitForModalToBeClosed-error.png');
            this.test.assert(false, 'Document modal not closed');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });

});
