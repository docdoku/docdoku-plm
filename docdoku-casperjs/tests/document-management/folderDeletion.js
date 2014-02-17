/*global casper*/
casper.test.begin('Delete a folder is available',3, function(){
    casper.thenOpen(authUrl);

    casper.then(function openDocumentManagement() {
        this.test.assertExists('#documents_management_link a', 'Documents management link found');
        this.click('#documents_management_link a');
    });

    casper.waitForSelector('#folder-nav',
        function createNewFolder() {
            this.test.assertExists('#folder-nav .nav-list-entry a', 'Folders link found');
            this.click("#folder-nav .nav-list-entry a");
        }
    );

    casper.waitForSelector('#folder-nav .items',
        function deleteNewFolder() {
            this.test.assertExists('#folder-nav .items a[title='+folderCreationName+']', 'Folder created found');
            this.click("#folder-nav .items a[title="+folderCreationName+"] + .btn-group .delete a");
        }
    );

    casper.run(function() {
        this.test.done();
    });
});