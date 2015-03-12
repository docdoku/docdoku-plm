/*global casper,urls,documents*/

casper.test.begin('LOV deletion tests suite',2, function documentLOVDeletionTestsSuite(){

    'use strict';

    casper.open('');

    /**
     * Open document management URL
     * */

    casper.then(function(){
        this.open(urls.documentManagement);
    });

    /**
     * Open template nav
     */

    casper.then(function waitForTemplateNavLink(){
        this.waitForSelector('#template-nav > .nav-list-entry > a',function clickTemplateNavLink() {
            this.click('#template-nav > .nav-list-entry > a');
        });
    });

    /**
     * Open LOV creation modal
     */

    casper.then(function waitForLOVCreationLink(){
        this.waitForSelector('.actions .list-lov',function clickOnLOVCreationLink(){
            this.click('.actions .list-lov');
        });
    });

    /**
     * Check the number of item
     */

    casper.then(function waitForLOVCreationModal(){
        this.waitForSelector('.list-lov',function checkLOVPersistence(){
            this.test.assertExists('.lovItem', "One element should be in the list of LOV");
        });
    });

    /**
     * Delete the item and save
     */
    casper.then(function deleteItem(){
        this.click('.deleteLovItem');
        this.click('.btn-saveLovs');
    });

    /**
     * Re-open the modal to check
     */
    casper.then(function reopenModal(){
        this.waitForSelector('.actions .list-lov',function clickOnLOVCreationLink(){
            this.click('.actions .list-lov');
        });
    });

    /**
     * Check the number of item
     */

    casper.then(function waitForLOVCreationModal(){
        this.waitForSelector('.list-lov',function checkDeletion(){
            this.waitWhileSelector('.lovItem',function templateDeleted(){
                this.test.assert(true,'LOV deleted');
            },function fail(){
                this.capture('screenshot/lov/lovDeletion-error.png');
                this.test.assert(false,'love still there');
            });
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});
/**
 * Created by lebeaujulien on 12/03/15.
 */
