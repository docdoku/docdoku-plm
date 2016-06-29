/*global casper,urls,workspace,documents*/
casper.test.begin('Document upload  file tests suite', 2, function documentUploadCadTestsSuite() {
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
            this.capture('screenshot/documentUpload/waitForFolderNavLink-error.png');
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
            this.capture('screenshot/documentUpload/waitForDocumentDisplayed-error.png');
            this.test.assert(false, 'Document can not be found');
        });
    });

    /**
     * Wait for document modal
     */

    casper.then(function waitForDocumentModal() {
        var modalTab = '.document-modal .tabs li a[href="#tab-iteration-files"]';
        return this.waitForSelector(modalTab, function modalOpened() {
            this.click(modalTab);
        }, function fail() {
            this.capture('screenshot/documentUpload/waitForDocumentModal-error.png');
            this.test.assert(false, 'Document modal can not be found');
        });
    });

    /**
     * Wait the modal tab
     */
    casper.then(function waitForDocumentModalTab() {
        return this.waitForSelector('.document-modal .upload-btn', function tabSelected() {
            this.test.assert(true, 'File upload tab opened');
        }, function fail() {
            this.capture('screenshot/documentUpload/waitForDocumentModalTab-error.png');
            this.test.assert(false, 'Upload button can not be found');
        });
    });
    /**
     * Choose a file and upload
     */
    casper.then(function setFileAndUpload() {
        this.fill('.document-modal .upload-form', {
            'upload': 'res/document-upload.txt'
        }, false);

        return this.waitFor(function checkFileHasBeenUploaded () {
            return this.evaluate(function () {
                return document.querySelectorAll('.document-modal .attachedFiles ul.file-list li').length === 1;
            });
        }, function then() {
            this.test.assert(true, 'File has been uploaded to document');
        }, function fail() {
            this.capture('screenshot/documentUpload/setFileAndUpload-error.png');
            this.test.assert(false, 'Cannot upload the file');
        });

    });

    casper.run(function allDone() {
        return this.test.done();
    });

});
