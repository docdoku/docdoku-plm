/*global casper,urls,products,baselines*/

casper.test.begin('Baseline duplication tests suite',2, function baselineDuplicationTestsSuite(){
    'use strict';

    casper.open('');

    /**
     * Open product management URL
     * */

    casper.then(function(){
        this.open(urls.productManagement);
    });

    /**
     * Go to baselines nav
     */
    casper.then(function waitForBaselineNavLink(){
        this.waitForSelector('#baselines-nav > .nav-list-entry > a',function clickBaselineNavLink() {
            this.click('#baselines-nav > .nav-list-entry > a');
        },function fail(){
            this.capture('screenshot/baselineDuplication/waitForBaselineNavLink-error.png');
            this.test.assert(false,'Baseline nav link can not be found');
        });
    });

    /**
     * Select the first baseline with checkbox
     */
    casper.then(function waitForBaselineTable(){
        this.waitForSelector('#baseline_table tbody tr:first-child  td:first-child input',function clickOnBaselineCheckbox() {
            this.click('#baseline_table tbody tr:first-child  td:first-child input');
        },function fail(){
            this.capture('screenshot/baselineDuplication/waitForBaselineTable-error.png');
            this.test.assert(false,'Baseline can not be found');
        });
    });

    /**
     * Click on baseline duplication button
     */
    casper.then(function waitForBaselineDuplicationButton(){
        this.waitForSelector('.actions .duplicate',function openBaselineDuplicationModal() {
            this.click('.actions .duplicate');
        },function fail() {
            this.capture('screenshot/baselineDuplication/waitForBaselineDuplicationButton-error.png');
            this.test.assert(false,'New baseline button can not be found');
        });
    });

    /**
     * Try to duplicate a baseline without a name
     */
    casper.then(function waitForBaselineDuplicationModal(){
        this.waitForSelector('#baseline_duplicate_modal .modal-footer .btn-primary',function baselineDuplicationModalOpened() {
            this.click('#baseline_duplicate_modal .modal-footer .btn-primary');
            this.test.assertExists('#baseline_duplicate_modal #inputBaselineName:invalid', 'Should not duplicate baseline without a name');
        },function fail() {
            this.capture('screenshot/baselineDuplication/waitForBaselineDuplicationModal-error.png');
            this.test.assert(false,'Duplicate baseline modal can not be found');
        });
    });

    /**
     * Try to duplicate the baseline
     */
    casper.then(function tryToDuplicateABaseline(){
        this.waitForSelector('#baseline_duplicate_modal #inputBaselineName',function fillBaselineDuplicationForm() {
            this.sendKeys('#baseline_duplicate_modal #inputBaselineName', baselines.baseline2.name, {reset:true});
            this.sendKeys('#baseline_duplicate_modal #inputBaselineDescription', baselines.baseline2.description, {reset:true});
            this.click('#baseline_duplicate_modal .modal-footer .btn-primary');
        },function fail() {
            this.capture('screenshot/baselineDuplication/tryToDuplicateABaseline-error.png');
            this.test.assert(false,'Cannot duplicate the baseline');
        });
    });

    /**
     * Wait for the modal to be closed
     */
    casper.then(function waitForModalToBeClosed(){
        this.waitWhileSelector('#baseline_duplicate_modal',function onBaselineDuplicationModalClosed(){
            this.test.assert(true,'Baseline duplication modal has been closed');
        },function fail() {
            this.capture('screenshot/baselineDuplication/waitForModalToBeClosed-error.png');
            this.test.assert(false,'Baseline duplication modal can not close');
        });
    });

    casper.run(function allDone(){
        this.test.done();
    });

});
