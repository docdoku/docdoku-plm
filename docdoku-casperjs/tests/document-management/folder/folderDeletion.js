/*global casper,__utils__,workspaceUrl,folderCreationName*/
'use strict';
casper.test.begin('Folder deletion tests suite',1, function folderDeletionTestsSuite(){

    /**
     * Open document management URL
     * */
    casper.open(documentManagementUrl);

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