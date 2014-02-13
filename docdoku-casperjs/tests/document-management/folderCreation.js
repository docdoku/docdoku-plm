/*global casper*/
casper.test.begin('Create a folder is available',4, function(){
    casper.thenOpen(deleteFolderUrl,{method: 'DELETE'});
    casper.thenOpen(authUrl);

    casper.then(function openDocumentManagement() {
        this.test.assertExists('#documents_management_link a', 'Documents management link found');
        this.click("#documents_management_link a");
    });

    casper.waitForSelector('#folder-nav',
        function createNewFolder() {
            this.test.assertExists('#folder-nav .new-folder', 'New folder link found');
            this.click("#folder-nav .new-folder a");
        }
    );

    casper.waitForSelector('#new-folder-form',
        function fillFolderName() {
            this.test.assertExists('#new-folder-form input', 'New folder input found');
            this.sendKeys('#new-folder-form input', folderCreationName);
            this.test.assertExists('.modal .btn-primary', 'Submit button found');
            this.click('.modal .btn-primary');
        }
    );

    casper.run(function() {
        this.test.done();
    });
});