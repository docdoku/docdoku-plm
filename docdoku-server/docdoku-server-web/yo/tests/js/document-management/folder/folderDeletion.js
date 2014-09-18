/*global casper*/

casper.test.begin('Folder deletion tests suite',1, function folderDeletionTestsSuite(){

    'use strict';

    casper.open('');

    /**
     * Open document management URL
     * */

    casper.then(function(){
        this.open(documentManagementUrl);
    });

    /**
     * Click on delete folder link
     */

    casper.then(function waitForDeleteFolderLink(){
        this.waitForSelector('#folder-nav .items a[title="'+folderCreationName+'"] + .btn-group .delete a',function clickFolderDeleteLink() {
            this.click('#folder-nav .items a[title="'+folderCreationName+'"] + .btn-group .delete a');
        });
    });

    /**
     * Test if folder has been deleted
     */

    casper.then(function waitForFolderDisappear(){
        this.waitWhileSelector('#folder-nav .items a[title='+folderCreationName+']',function folderHasBEenDeleted(){
            this.test.assert(true,'Folder deleted');
        })
    });

    casper.run(function allDone() {
        this.test.done();
    });
});