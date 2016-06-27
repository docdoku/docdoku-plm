/*global casper,urls,documents*/

casper.test.begin('Folder deletion tests suite', 1, function folderDeletionTestsSuite() {

    'use strict';

    casper.open('');

    /**
     * Open document management URL
     * */

    casper.then(function () {
        this.open(urls.documentManagement);
    });

    /**
     * Click on delete folder link
     */

    casper.then(function waitForDeleteFolderLink() {
        this.waitForSelector('#folder-nav .items a[title="' + documents.folder1 + '"] + .btn-group .delete a', function clickFolderDeleteLink() {
            this.click('#folder-nav .items a[title="' + documents.folder1 + '"] + .btn-group .delete a');
        }, function fail() {
            this.capture('screenshot/folderDeletion/waitForDeleteFolderLink-error.png');
            this.test.assert(false, 'Folder link not found');
        });
    });


    /**
     * Confirm folder deletion
     */

    casper.then(function confirmFolderDeletion() {
        this.waitForSelector('.bootbox', function confirmBoxAppeared() {
            this.click('.bootbox .modal-footer .btn-primary');
        }, function fail() {
            this.capture('screenshot/folderDeletion/confirmFolderDeletion-error.png');
            this.test.assert(false, 'Folder deletion confirmation modal can not be found');
        });
    });

    /**
     * Test if folder has been deleted
     */

    casper.then(function waitForFolderDisappear() {
        this.waitWhileSelector('#folder-nav .items a[title=' + documents.folder1 + ']', function folderHasBEenDeleted() {
            this.test.assert(true, 'Folder deleted');
        }, function fail() {
            this.capture('screenshot/folderDeletion/waitForFolderDisappear-error.png');
            this.test.assert(false, 'Folder still there');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});
