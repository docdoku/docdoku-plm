/*global casper,urls,products*/

casper.test.begin('Public shared part tests suite', 2, function publicSharedPartTestsSuite() {

    'use strict';

    casper.open('');

    /**
     * Open part management URL
     * */

    casper.then(function () {
        this.open(urls.partPermalink);
    });

    /**
     * Check for part title
     */

    casper.then(function checkPartTitle() {
        this.waitForSelector('#page > h3', function titleDisplayed() {
            this.test.assertSelectorHasText('#page > h3', products.part1.number + '-A');
        }, function fail() {
            this.capture('screenshot/publicSharedPart/checkPartTitle-error.png');
            this.test.assert(false, 'Title can not be found');
        });
    });

    /**
     * Check for part iteration note
     */
    casper.then(function checkIterationNote() {
        this.click('.nav-tabs a[href="#tab-part-iteration"]');
        this.waitForSelector('#page > h3', function iterationNoteDisplayed() {
            this.test.assertSelectorHasText('#tab-part-iteration > table > tbody > tr:nth-child(2) > td', products.part1.iterationNote);
        }, function fail() {
            this.capture('screenshot/publicSharedPart/checkIterationNote-error.png');
            this.test.assert(false, 'Iteration note can not be found');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});
