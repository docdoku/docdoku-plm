/*global casper,__utils__,workspaceUrl,folderCreationName*/
'use strict';
casper.test.begin('Create a folder is available',1, function(){
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
     * Open new folder modal
     */
    casper.waitForSelector('#folder-nav',function openNewFolderModal(){
            exists = this.evaluate(function() {
                return __utils__.exists('#folder-nav .new-folder a');
            });
            if(!exists){
                this.test.fail('New folder link not found');
                this.exit('New folder not found');
            }
            this.evaluate(function(){__utils__.log('New folder link found', 'info');});
            this.click('#folder-nav .new-folder a');
    });

    /**
     * Test create a folder
     */
    casper.waitForSelector('#new-folder-form',function fillNewFolderForm(){
        // Search Folder Name Input
        exists = this.evaluate(function() {
            return __utils__.exists('#new-folder-form input');
        });
        if(!exists){
            this.test.fail('New folder input not found');
            this.exit('New folder input not found');
        }
        this.evaluate(function(){__utils__.log('New folder input found', 'info');});
        this.sendKeys('#new-folder-form input', folderCreationName, {reset:true});

        // Search Part Creation Submit Button
        exists = this.evaluate(function() {
            return __utils__.exists('.modal .btn-primary');
        });
        if(!exists){
            this.test.fail('Folder creation submit button missing');
            this.exit('Folder creation submit button missing');
        }
        this.evaluate(function(){__utils__.log('Folder creation submit button found', 'info');});
        this.click('.modal .btn-primary');

        this.wait(1000, function (){
            this.test.assertDoesntExist('.modal .btn-primary', 'Should creating the folder '+folderCreationName);
        });
    });

    casper.run(function() {
        this.test.done();
    });
});