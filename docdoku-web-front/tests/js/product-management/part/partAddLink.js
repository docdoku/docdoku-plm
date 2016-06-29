/*global casper,urls,products*/
casper.test.begin('Part add link tests suite', 2, function partAddLinkTestsSuite() {
    'use strict';

    casper.open('');

    /**
     * Open product management URL
     * */

    casper.then(function () {
        return this.open(urls.productManagement);
    });

    /**
     * Go to part nav
     */
    casper.then(function waitForPartNavLink() {
        return this.waitForSelector('#part-nav > .nav-list-entry > a', function clickPartNavLink() {
            this.click('#part-nav > .nav-list-entry > a');
        }, function fail() {
            this.capture('screenshot/partAddLink/waitForPartNavLink-error.png');
            this.test.assert(false, 'Part nav link can not be found');
        });
    });

    /**
     * Wait for part list display
     */

    casper.then(function waitForPartInList() {
        return this.waitForSelector('#part_table tbody tr:first-child td.part_number', function clickOnPartCheckbox() {
            this.click('#part_table tbody tr:first-child td.part_number span');
        }, function fail() {
            this.capture('screenshot/partAddLink/waitForPartList-error.png');
            this.test.assert(false, 'Part list can not be found');
        });
    });

    /**
     * Wait for part modal
     */

    casper.then(function waitForPartModal() {
        var modalTab = '#part-modal .tabs li a[href="#tab-part-links"]';

        return this.waitForSelector(modalTab, function modalOpened() {
            this.click(modalTab);
        }, function fail() {
            this.capture('screenshot/partAddLink/waitForPartModal-error.png');
            this.test.assert(false, 'Part modal can not be found');
        });
    });

    /**
     * Wait for Links modal tab
     */
    casper.then(function waitForPartModalLinksTab() {
        return this.waitForSelector('#part-modal .linked-items-reference-typehead', function tabOpened() {
            this.test.assert(true, 'Links tab opened');
        }, function fail() {
            this.capture('screenshot/partAddLink/waitForPartModalLinksTab-error.png');
            this.test.assert(false, 'Part modal Links tab can not be found');
        });
    });

    /**
     * Wait for parts select list
     */
    casper.then(function waitForDocumentsSelectList() {
        this.sendKeys('#part-modal .linked-items-reference-typehead', products.part1.documentLink, {reset: true});

        return this.waitForSelector('#iteration-links > .linked-items-view > ul.dropdown-menu > li:first-child', function documentsSelectListDisplayed() {
            this.click('#iteration-links > .linked-items-view > ul.dropdown-menu > li:first-child');
        }, function fail() {
            this.capture('screenshot/partAddLink/waitForDocumentsSelectList-error.png');
            this.test.assert(false, 'Documents select list can not be found');
        });
    });

    /**
     * Wait for linked document display
     */
    casper.then(function waitForLinkedDocumentDisplay() {
        return this.waitForSelector('#iteration-links > .linked-items-view > ul.linked-items > li:first-child', function linkDocumentDisplayed() {
            this.test.assert(true, 'Link added');
            this.click('#part-modal .btn.btn-primary');
        }, function fail() {
            this.capture('screenshot/partAddLink/waitForLinkedDocumentDisplay-error.png');
            this.test.assert(false, 'Linked document can not be found and saved');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });

});
