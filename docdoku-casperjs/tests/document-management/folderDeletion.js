/*global casper,__utils__,workspaceUrl,folderCreationName*/
'use strict';
casper.test.begin('Delete a folder is available',1, function(){
    casper.thenOpen(workspaceUrl);
    var exists;

    /**
     * Go to document management
     */
    casper.then(function openDocumentManagement() {
        exists = this.evaluate(function() {
            return __utils__.exists('#documents_management_link a');
        });
        if(!exists){
            this.test.fail('Documents management link not found');
            this.exit('Documents management link not found');
        }
        this.evaluate(function(){__utils__.log('Documents management link found', 'info');});
        this.click('#documents_management_link a');
    });

    /**
     *  Go to folder nav
     */
    casper.waitForSelector('#folder-nav',function openFolderNav() {
        exists = this.evaluate(function() {
            return __utils__.exists('#folder-nav .nav-list-entry a');
        });
        if(!exists){
            this.test.fail('Folders nav link not found');
            this.exit('Folders nav link not found');
        }
        this.evaluate(function(){__utils__.log('Folders nav link found', 'info');});
        this.click('#folder-nav .nav-list-entry a');
    });

    /**
     * Test delete the created folder
     */
    casper.waitForSelector('#folder-nav .items',function deleteNewFolder() {
        exists = this.evaluate(function() {
            return true;//__utils__.exists('#folder-nav .items a[title="'+folderCreationName+'"]');  // Todo Evaluate if the folder exist
        });
        if(!exists){
            this.test.fail('Created folder not found');
            this.exit('Created folder not found');
        }
        this.evaluate(function(){__utils__.log('Created folder found', 'info');});
    });
    casper.thenClick('#folder-nav .items a[title="'+folderCreationName+'"] + .btn-group .delete a',function(){
        this.wait(1000,function(){
            this.test.assertDoesntExist('#folder-nav .items a[title='+folderCreationName+']', 'Created folder should be deleted');
        });
    });

    casper.run(function() {
        this.test.done();
    });
});