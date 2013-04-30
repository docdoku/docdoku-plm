casper.thenOpen(authUrl);

casper.then(function openDocumentManagement() {
    this.test.assertExists('#documents_link a', 'Documents management link found');
    this.click("#documents_link a");
});

casper.waitForSelector('#folder-nav',
    function createNewFolder() {
        this.test.assertExists('#folder-nav .nav-list-entry a', 'Folders link found');
        this.click("#folder-nav .nav-list-entry a");
    }
);

casper.waitForSelector('#items-view7',
    function deleteNewFolder() {
        this.test.assertExists('#items-view7 li:nth-child(2)', 'Folder deletion link found');
        this.click("#items-view7 li:nth-child(2) .delete a");
    }
);

casper.run(function() {
    this.test.done(3);
});