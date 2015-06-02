/**
 * Created by kelto on 02/06/15.
 */

casper.test.begin('Part template attributes tests suite', 10, function partTemplateCreationTestsSuite() {
    'use strict';

    casper.open('');

    /**
     * Open product management URL
     * */

    casper.then(function () {
        this.open(urls.productManagement);
    });

    casper.then(function waitForPartTemplateNavLink() {
        this.waitForSelector('#part-template-nav > .nav-list-entry > a', function clickPartTemplateNavLink() {
            this.click('#part-template-nav > .nav-list-entry > a');
        }, function fail() {
            this.capture('screenshot/partTemplateCreation/waitForPartTemplateNavLink-error.png');
            this.test.assert(false, 'Part template nav link can not be found');
        });
    });

    /**
     * Wait for the creation button
     */
    casper.then(function waitForPartTemplateContent() {
        //should select the one created
        this.waitForSelector('#part_template_table tbody tr:first-child td.reference', function clickPartTemplateCreationButton() {
            this.click('#part_template_table tbody tr:first-child td.reference');
        }, function fail() {
            this.capture('screenshot/partTemplateCreation/waitForPartTemplateCreationButton-error.png');
            this.test.assert(false, 'Part template creation button can not be found');
        });
    });

    casper.then(function () {
        var attributesTabSelector = '.nav.nav-tabs > li:nth-child(3) > a';
        this.waitForSelector(attributesTabSelector, function () {
            this.click(attributesTabSelector);
            this.waitForSelector('.nav.nav-tabs > li:nth-child(3).active', function () {
                var addAttributeButtonSelector = '.btn.add';
                this.waitForSelector(addAttributeButtonSelector, function () {
                    this.click(addAttributeButtonSelector);
                }, function () {
                    this.capture('screenshot/attributes/attributeTabBecomeActive-error.png');
                    this.test.assert(false, 'Add attribute button not found');
                });
            }, function () {
                this.capture('screenshot/attributes/attributeTabBecomeActive-error.png');
                this.test.assert(false, 'Attribute tab not appearing');
            });
        }, function () {
            this.capture('screenshot/attributes/clickOnAttributeTab-error.png');
            this.test.assert(false, 'Attribute tab cannot be found');
        });
    });

    /**
     * Test the lock/freeze/mandatory checkboxe
     */
    casper.then(function testFreezeAttribute() {
        this.waitForSelector('.lock', function () {
            this.test.assert(true, 'lock checkbox present');
        }, function fail() {
            this.capture('screenshot/attributes/all-lock-not-found.png');
            this.test.assert(false, 'the lock all checkbox should exist');
        });
        //test that the attribute locked checkbox exist
        this.waitForSelector('.attribute-locked', function () {
            this.test.assert(true, 'attribute-locked present');
        }, function fail() {
            this.capture('screenshot/attributes/attribute-lock-not-found.png');
            this.test.assert(false, 'checkbox attribute locked should exist');
        });
        this.waitForSelector('.attribute-mandatory', function () {
            this.test.assert(true, 'attribute-mandatory present');
        }, function fail() {
            this.capture('screenshot/attributes/attribute-mandatory-not-found.png');
            this.test.assert(false, 'checkbox attribute mandatory should exist');
        });

        this.waitForSelector('.lock', function () {
            this.test.assert(true, 'lock checkbox present');
            this.click('.lock');
        }, function fail() {
            this.test.assert(false, 'lock checkbox should exist');
        });

        this.waitWhileSelector('.attribute-locked', function () {
            this.test.assert(true, 'attribute-locked removed');
        }, function fail() {
            this.test.assert(false, 'attribute-locked should have been removed');
        });
        this.waitForSelector('.attribute-mandatory', function () {
            this.test.assert(true, 'attribute-mandatory present');
            this.click('.attribute-mandatory');
            this.test.assertDoesntExist('.attribute-locked', 'checkbox attribute locked should not exist');
        }, function fail() {
            this.test.assert(false, 'attribute-mandatory should exist');
        });

        this.waitForSelector('.lock', function () {
            this.test.assert(true, 'lock checkbox present');
            this.click('.lock');
            this.waitForSelector('.attribute-locked', function () {
                this.test.assert(true, 'attribute-locked exist');
            }, function fail() {
                this.test.assert(false, 'attribute-locked should exist');
            });
        }, function fail() {
            this.test.assert(false, 'lock checkbox should exist');
        });

    });
    casper.then(function () {
        this.waitForSelector('.list-item.well', function () {
            this.test.assertElementCount('.list-item.well', 1);
            this.click('.btn.btn-primary');
        }, function () {
            this.capture('screenshot/attributes/addAttribute-error.png');
            this.test.assert(false, 'Attribute not appearing in the list');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});
