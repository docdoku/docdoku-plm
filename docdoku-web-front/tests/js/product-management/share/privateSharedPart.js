/*global casper,urls,products*/

casper.test.begin('Private shared part tests suite', 3, function privateSharedPartTestsSuite() {

    'use strict';

    var titleSelector = '#content > .part-revision > div >  h3';

    casper.open('');

    /**
     * Open part management URL
     * */

    casper.then(function () {
        return this.open(urls.privatePartPermalink);
    });

    /**
     * We should be prompted for password
     */
    casper.then(function checkPasswordIsRequested() {
        return this.waitForSelector('#prompt_modal #prompt_input.ready', function passwordRequested() {
            this.sendKeys('#prompt_modal #prompt_input', products.part1.sharedPassword, {reset: true});
            this.click('#submitPrompt');
            this.test.assert(true, 'We are prompted for password');
        }, function fail() {
            this.capture('screenshot/privateSharedPart/checkPasswordIsRequested-error.png');
            this.test.assert(false, 'Password field can not be found');
        });
    });

    /**
     * Check for part title
     */

    casper.then(function checkPartTitle() {
        return this.waitForSelector(titleSelector, function titleDisplayed() {
            this.test.assertSelectorHasText(titleSelector, products.part1.number + '-A');
        }, function fail() {
            this.capture('screenshot/privateSharedPart/checkPartTitle-error.png');
            this.test.assert(false, 'Title can not be found');
        });
    });

    /**
     * Check for part iteration note
     */
    casper.then(function checkIterationNote() {
        this.click('.nav-tabs a[href="#tab-part-iteration"]');
        return this.waitForSelector(titleSelector, function iterationNoteDisplayed() {
            this.test.assertSelectorHasText('#tab-part-iteration > table > tbody > tr:nth-child(2) > td', products.part1.iterationNote);
        }, function fail() {
            this.capture('screenshot/privateSharedPart/checkIterationNote-error.png');
            this.test.assert(false, 'Iteration note can not be found');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });
});
