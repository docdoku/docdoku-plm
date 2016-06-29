/*global casper,urls,documents*/

casper.test.begin('Private shared document tests suite', 3, function privateSharedDocumentTestsSuite() {

    'use strict';

    var titleSelector = '#content > .document-revision > div >  h3';

    casper.open('');

    /**
     * Open document management URL
     * */

    casper.then(function () {
        return this.open(urls.privateDocumentPermalink);
    });

    /**
     * We should be prompted for password
     */
    casper.then(function checkPasswordIsRequested() {
        return this.waitForSelector('#prompt_modal #prompt_input.ready', function passwordRequested() {
            this.sendKeys('#prompt_modal #prompt_input', documents.document1.sharedPassword, {reset: true});
            this.click('#submitPrompt');
            this.test.assert(true, 'We are prompted for password');
        }, function fail() {
            this.capture('screenshot/privateSharedDocument/checkPasswordIsRequested-error.png');
            this.test.assert(false, 'Password field can not be found');
        });
    });

    /**
     * Check for document title
     */

    casper.then(function checkDocumentTitle() {
        return this.waitForSelector(titleSelector, function titleDisplayed() {
            this.test.assertSelectorHasText(titleSelector, documents.document1.number + '-A');
        }, function fail() {
            this.capture('screenshot/privateSharedDocument/checkDocumentTitle-error.png');
            this.test.assert(false, 'Title can not be found');
        });
    });

    /**
     * Check for document iteration note
     */
    casper.then(function checkIterationNote() {
        this.click('.nav-tabs a[href="#tab-document-iteration"]');
        return this.waitForSelector(titleSelector, function iterationNoteDisplayed() {
            this.test.assertSelectorHasText('#tab-document-iteration > table > tbody > tr:nth-child(2) > td', documents.document1.iterationNote);
        }, function fail() {
            this.capture('screenshot/privateSharedDocument/checkIterationNote-error.png');
            this.test.assert(false, 'Iteration note can not be found');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });
});
