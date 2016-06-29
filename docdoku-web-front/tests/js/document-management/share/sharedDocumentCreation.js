/*global casper,urls,workspace,documents*/

casper.test.begin('Shared document creation tests suite', 7, function sharedDocumentCreationTestsSuite() {

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
            this.capture('screenshot/sharedDocumentCreation/waitForFolderNavLink-error.png');
            this.test.assert(false, 'Folder nav link can not be found');
        });
    });

    /**
     * Find the shared document link in the document list
     */

    casper.then(function waitForDocumentList() {
        var link = '#document-management-content table.dataTable tbody tr:first-child td.document-master-share i';
        return this.waitForSelector(link, function onLinkFound() {
            this.click(link);
        }, function fail() {
            this.capture('screenshot/sharedDocumentCreation/waitForFolderNavLink-error.png');
            this.test.assert(false, 'Shared document modal can not be found');
        });
    });

    /**
     * Wait for modal
     */
    casper.then(function waitForSharedDocumentCreationModal() {
        return this.waitForSelector('#share-modal', function modalOpened() {
            this.test.assert(true, 'Shared document modal is opened');
        }, function fail() {
            this.capture('screenshot/sharedDocumentCreation/waitForSharedDocumentCreationModal-error.png');
            this.test.assert(false, 'Shared document modal can not be found');
        });
    });

    /**
     * Set the document as public
     */
    casper.then(function setDocumentAsPublicShared() {

        this.click('#share-modal .public-shared-switch .switch-off input');
        return this.waitForSelector('#share-modal .public-shared-switch .switch-on', function publicSharedCreated() {
            this.test.assert(true, 'Document is now public shared');
        }, function fail() {
            this.capture('screenshot/sharedDocumentCreation/setDocumentAsPublicShared-error.png');
            this.test.assert(false, 'Shared document cannot be shared as public');
        });

    });


    /**
     * Create a private share, with expire date and password
     */
    casper.then(function createDocumentPrivateShare() {

        this.sendKeys('#private-share .password', documents.document1.sharedPassword, {reset: true});
        this.sendKeys('#private-share .confirm-password', documents.document1.sharedPassword, {reset: true});
        this.sendKeys('#private-share .expire-date', documents.document1.expireDate, {reset: true});

        this.click('#private-share #generate-private-share');

        return this.waitForSelector('#private-share > div > div > a', function onLinkGenerated() {
            var url = this.fetchText('#private-share > div > div > a');
            urls.privateDocumentPermalink = url;
            this.test.assert(true, 'Private share created : ' + url);
        }, function fail() {
            this.capture('screenshot/sharedDocumentCreation/createDocumentPrivateShare-error.png');
            this.test.assert(false, 'Shared document cannot be shared as private');
        });
    });


    /**
     * Close the modal
     */
    casper.then(function closeSharedDocumentModal() {

        this.click('#share-modal > div.modal-header > button');

        return this.waitWhileSelector('#share-modal', function modalClosed() {
            this.test.assert(true, 'Shared document modal closed');
        }, function fail() {
            this.capture('screenshot/sharedDocumentCreation/closeSharedDocumentModal-error.png');
            this.test.assert(false, 'Shared document modal cannot be closed');
        });

    });

    /**
     * Reopen the modal to create a second private share, expired one.
     */
    casper.then(function waitForDocumentList() {
        var link = '#document-management-content table.dataTable tbody tr:first-child td.document-master-share i';
        return this.waitForSelector(link, function onLinkFound() {
            this.click(link);
        }, function fail() {
            this.capture('screenshot/sharedDocumentCreation/waitForDocumentList-error.png');
            this.test.assert(false, 'Shared document modal can not be found');
        });
    });

    /**
     * Wait for modal
     */
    casper.then(function waitForSharedDocumentCreationModal() {
        return this.waitForSelector('#share-modal', function modalOpened() {

            this.test.assert(true, 'Shared document modal is opened');

            this.sendKeys('#private-share .password', documents.document1.sharedPassword, {reset: true});
            this.sendKeys('#private-share .confirm-password', documents.document1.sharedPassword, {reset: true});
            this.sendKeys('#private-share .expire-date', documents.document1.expireDate2, {reset: true});
            this.click('#private-share #generate-private-share');

        }, function fail() {
            this.capture('screenshot/sharedDocumentCreation/waitForSharedDocumentCreationModal-error.png');
            this.test.assert(false, 'Shared document modal can not be found');
        });
    });

    /**
     * Save the generated url for test later
     */
    casper.then(function createDocumentPrivateShare() {
        return this.waitForSelector('#private-share > div > div > a', function onLinkGenerated() {
            var url = this.fetchText('#private-share > div > div > a');
            urls.privateDocumentPermalinkExpired = url;
            this.test.assert(true, 'Private share created expiring yesterday : ' + url);
        }, function fail() {
            this.capture('screenshot/sharedDocumentCreation/createDocumentPrivateShare-error.png');
            this.test.assert(false, 'Shared document cannot be shared as private');
        });

    });

    /**
     * Close the modal
     */
    casper.then(function closeSharedDocumentModal() {

        this.click('#share-modal > div.modal-header > button');

        return this.waitWhileSelector('#share-modal', function modalClosed() {
            this.test.assert(true, 'Shared document modal closed');
        }, function fail() {
            this.capture('screenshot/sharedDocumentCreation/closeSharedDocumentModal-error.png');
            this.test.assert(false, 'Shared document modal cannot be closed');
        });

    });


    casper.run(function allDone() {
        this.test.done();
    });

});
