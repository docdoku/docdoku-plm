/*global casper,urls,workspace,products,defaultUrl*/
casper.test.begin('Part add link tests suite', 2, function partClickLinkTestsSuite() {
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
            this.capture('screenshot/partClickLink/waitForPartNavLink-error.png');
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
            this.capture('screenshot/partClickLink/waitForPartList-error.png');
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
            this.capture('screenshot/partClickLink/waitForPartModal-error.png');
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
            this.capture('screenshot/partClickLink/waitForPartModalLinksTab-error.png');
            this.test.assert(false, 'Part modal Links tab can not be found');
        });
    });

    /**
     * Wait for linked document display
     */
    casper.then(function waitForLinkedDocumentDisplay() {
        return this.waitForSelector('#iteration-links > .linked-items-view > ul.linked-items > li:first-child', function linkDocumentDisplayed() {
            this.click('#iteration-links > .linked-items-view > ul.linked-items > li:first-child > a.reference');
        }, function fail() {
            this.capture('screenshot/partClickLink/waitForLinkedDocumentDisplay-error.png');
            this.test.assert(false, 'Linked document can not be found');
        });
    });

    /**
     * Wait for linked document modal
     */
    casper.then(function waitForLinkedDocumentDisplay() {
        var modalTitle = '.document-modal > .modal-header > h3 > a[href="' + defaultUrl + '/documents/#' + workspace + '/' + products.part1.documentLink +'/A"]';

        return this.waitForSelector(modalTitle, function linkedModalOpened() {
            this.test.assert(true, 'Linked document modal opened');
        }, function fail() {
            this.capture('screenshot/partClickLink/waitForLinkedDocumentModal-error.png');
            this.test.assert(false, 'Linked document modal can not be found');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });

});
