/*global casper,urls,documents*/

casper.test.begin('Public shared document tests suite', 2, function publicSharedDocumentTestsSuite() {

    'use strict';

    casper.open('');

    /**
     * Open document management URL
     * */

    casper.then(function () {
        this.open(urls.documentPermalink);
    });

    /**
     * Check for document title
     */

    casper.then(function checkDocumentTitle() {
        this.waitForSelector('#content > .document-revision > div >  h3', function titleDisplayed() {
            this.test.assertSelectorHasText('#content > .document-revision > div >  h3', documents.document1.number + '-A');
        }, function fail() {
            this.capture('screenshot/publicSharedDocument/checkDocumentTitle-error.png');
            this.test.assert(false, 'Title can not be found');
        });
    });

    /**
     * Check for document iteration note
     */
    casper.then(function checkIterationNote() {
        this.click('.nav-tabs a[href="#tab-document-iteration"]');
        this.waitForSelector('#content > .document-revision > div >  h3', function iterationNoteDisplayed() {
            this.test.assertSelectorHasText('#tab-document-iteration > table > tbody > tr:nth-child(2) > td', documents.document1.iterationNote);
        }, function fail() {
            this.capture('screenshot/publicSharedDocument/checkIterationNote-error.png');
            this.test.assert(false, 'Iteration note can not be found');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});
