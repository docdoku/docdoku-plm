/*global casper,urls,products,$*/

casper.test.begin('Query builder search tests suite', 5, function queryBuilderSearchTestsSuite() {
    'use strict';

    casper.open('');

    /**
     * Open product management URL
     * */

    casper.then(function () {
        this.open(urls.productManagement + '/parts');
    });

    /**
     * Wait for query builder button
     * */
    casper.then(function waitForQueryBuilderDisplayButton() {
        this.waitForSelector('#part-search-form .display-query-builder-button', function clickOnQueryBuilderDisplayButton() {
            this.click('#part-search-form .display-query-builder-button');
        }, function fail() {
            this.capture('screenshot/queryBuilderSearch/waitForQueryBuilderDisplayButton-error.png');
            this.test.assert(false, 'Query builder display button can not be found');
        });
    });

    /**
     * Wait for query builder display
     * */
    casper.then(function waitForQueryBuilderDisplay() {
        this.waitForSelector('#query-builder-view', function queryBuilderDisplayed() {
            this.test.assert(true, 'Query builder view displayed');
        }, function fail() {
            this.capture('screenshot/queryBuilderSearch/waitForQueryBuilderDisplay-error.png');
            this.test.assert(false, 'Query builder view can not be found');
        });
    });

    /**
     * Fill "select" field
     * */
    casper.then(function fillSelectField() {
        this.evaluate(function () {
            $('.query-select-block .selectize-input').click();
            setTimeout(function () {
                $('div.query-select-block  div.selectize-dropdown div[data-value="pm.number"]').click();
                $('div.query-select-block  div.selectize-dropdown div[data-value="pm.name"]').click();
            }, 0);
            return true;
        });
    });

    /**
     * Fill condition and
     * */

    casper.then(function fillSelectField() {
        this.evaluate(function () {
            $('#where_rule_0 > div.rule-filter-container > select').val('attr-TEXT.CasperJsTestAttr-lock');
            $('#where_rule_0 > div.rule-filter-container > select').change();
            return true;
        });
        this.sendKeys('#where_rule_0 > div.rule-value-container > input', products.part1.attributeValue);
    });


    /**
     * Run query
     * */

    casper.then(function fillSelectField() {
        this.click('#query-builder-view > div.query-actions-block >.search-button');
    });

    /**
     * Wait for result table
     * */

    casper.then(function waitForResultTable() {
        this.waitForSelector('#query-table table', function resultTableDisplayed() {
            this.test.assertElementCount('#query-table table tbody tr', 1, 'We should have one result matching our query');
            this.test.assertSelectorHasText('#query-table table tbody tr td span', products.part1.number, 'We should have ' + products.part1.number + ' in the first cell');
        }, function fail() {
            this.capture('screenshot/queryBuilderSearch/waitForQueryBuilderDisplay-error.png');
            this.test.assert(false, 'Query builder view can not be found');
        });
    });


    /**
     * Save query
     * */

    casper.then(function saveQuery() {
        this.click('#query-builder-view > div.query-actions-block > .save-button');
        this.waitForSelector('#prompt_modal.in', function promptDisplayed() {
            this.sendKeys('#prompt_modal #prompt_input','myQuery');
            this.click('#prompt_modal #submitPrompt');
        }, function fail() {
            this.capture('screenshot/queryBuilderSearch/saveQuery-error.png');
            this.test.assert(false, 'Query builder prompt for save not displayed');
        });
    });

    /**
    * Wait for modal to close
    * */

    casper.then(function saveQuery() {
        this.waitWhileSelector('#prompt_modal', function promptModalClosed() {
            this.test.assert(true,'Prompt modal has been closed');
        }, function fail() {
            this.capture('screenshot/queryBuilderSearch/saveQuery-error.png');
            this.test.assert(false, 'Query builder prompt for save not closed');
        });
    });

    /**
    * Check in the list if query has been saved
    * */

    casper.then(function onModalClosed() {
        this.waitForSelector('#query-builder-view > div.query-builder-body > div:nth-child(2) > select > option:nth-child(2)', function querySaved() {
            this.test.assertSelectorHasText('#query-builder-view > div.query-builder-body > div:nth-child(2) > select > option:nth-child(2)','myQuery', 'Query has been saved');
        }, function fail() {
            this.capture('screenshot/queryBuilderSearch/onModalClosed-error.png');
            this.test.assert(false, 'Query builder query not saved');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });

});
