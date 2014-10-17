/*global casper*/

casper.test.begin('Document creation tests suite',2, function documentCreationTestsSuite(){

    'use strict';

    casper.open('');

    /**
     * Open change management URL
     * */

    casper.then(function(){
        this.open(urls.changeManagement);
    });

    /**
     * Open change issues nav
     */
    casper.then(function waitForChangeIssuesNavLink(){
        this.waitForSelector('a[href="#'+workspace+'/issues"]',function(){
            this.click('a[href="#'+workspace+'/issues"]');
        });
    });

    /**
     * Open folder creation modal
     */
    casper.then(function clickOnChangeIssueCreationLink(){
        this.click('.actions .new-issue');
    });

    /**
     * Wait for modal, and try to create an empy issue
     */
    casper.then(function waitForChangeIssueCreationModal(){
        this.waitForSelector('#issue_creation_modal',function changeIssueModalCreationOpened(){
            this.click('#issue_creation_modal .btn.btn-primary');
            this.test.assertExists('#issue_creation_modal input#inputIssueName:invalid', 'Should not create an issue without a name');
        });
    });

    /**
     * Fill the form and create the issue
     */
    casper.then(function fillAndSubmitChangeIssueCreationModal(){
        this.waitForSelector('#issue_creation_modal input#inputIssueName',function(){
            this.sendKeys('#issue_creation_modal input#inputIssueName',changeItems.changeIssue1.number);
            this.click('#issue_creation_modal .btn.btn-primary');
        });
    });

    /**
     * Check if issue has been created
     */
    casper.then(function checkForDocumentCreation(){
        this.waitForSelector('#issue_table tr td.reference',function issueHasBeenCreated(){
            this.test.assertSelectorHasText('#issue_table tr td.reference',changeItems.changeIssue1.number);
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});