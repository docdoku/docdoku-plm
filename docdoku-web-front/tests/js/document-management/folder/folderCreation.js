/*global casper,urls,workspace,documents*/

casper.test.begin('Folder creation tests suite', 1, function folderCreationTestsSuite() {

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
        return this.waitForSelector('#folder-nav > .nav-list-entry > a', function clickFolderNavLink() {
            this.click('#folder-nav > .nav-list-entry > a');
        });
    });

    /**
     * Open folder creation modal
     */
    casper.then(function clickOnFolderCreationLink() {
        this.click('#folder-nav > div.nav-list-entry > div.btn-group > ul.dropdown-menu > li.new-folder > a');
        return this.waitForSelector('#new-folder-form', function openFolderCreationModal() {
            this.sendKeys('#new-folder-form input', documents.folder1, {reset: true});
            this.click('button[form=new-folder-form]');
        });
    });

    /**
     *  Check if folder has been created
     * */
    casper.then(function checkIfFolderHasBeenCreated() {
        return this.waitForSelector('a[href="#' + workspace + '/folders/' + documents.folder1 + '"]', function () {
            this.test.assert(true, 'Folder has been created');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });
});
