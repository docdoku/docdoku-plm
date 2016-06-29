/*global casper,urls*/

/**
 * @author lebeaujulien on 12/03/15.
 */

casper.test.begin('LOV deletion tests suite', 3, function documentLOVDeletionTestsSuite() {

    'use strict';

    casper.open('');

    /**
     * Open document management URL
     * */

    casper.then(function () {
        return this.open(urls.documentManagement);
    });

    /**
     * Open template nav
     */

    casper.then(function waitForTemplateNavLink() {
        return this.waitForSelector('#template-nav > .nav-list-entry > a', function clickTemplateNavLink() {
            this.click('#template-nav > .nav-list-entry > a');
        });
    });

    /**
     * Open LOV creation modal
     */

    casper.then(function waitForLOVCreationLink() {
        return this.waitForSelector('.actions .list-lov', function clickOnLOVCreationLink() {
            this.click('.actions .list-lov');
        });
    });

    /**
     * Check the number of item
     */

    casper.then(function waitForLOVCreationModal() {
        return this.waitForSelector('.modal.list_lov .lovItem', function checkLOVPersistence() {
            this.test.assertElementCount('.lovItem', 1, 'One element should be in the list of LOV');
        }, function(){
            this.capture('screenshot/lovDeletion/waitForLOVCreationModal-error.png');
        });
    });

    /**
     * Delete the item and save
     */
    casper.then(function deleteItem() {
        this.click('.deleteLovItem');
        this.click('.btn-saveLovs');
        return this.waitWhileSelector('.modal', function () {
            this.test.assert(true, 'modal closed');
        });
    });

    /**
     * Re-open the modal to check
     */
    casper.then(function reopenModal() {
        this.waitForSelector('.actions .list-lov', function clickOnLOVCreationLink() {
            this.click('.actions .list-lov');
        });
    });

    /**
     * Check the number of item
     */

    casper.then(function waitForLOVCreationModal() {
        return this.waitForSelector('.modal.list_lov .list_of_lov', function checkDeletion() {
            this.test.assertDoesntExist('.lovItem', 'LOV should be deleted');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });
});
