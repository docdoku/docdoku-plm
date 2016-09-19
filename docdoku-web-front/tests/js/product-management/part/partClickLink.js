/*global casper,urls,workspace,products,defaultUrl*/
casper.test.begin('Part click link tests suite', 3, function partClickLinkTestsSuite() {
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
     * Wait for part modal closed
     */
    casper.then(function waitForPartModalClosed() {
        var modalTitle = '#part-modal > .modal-header > h3 > a[href="' + contextPath + '/documents/#' + workspace + '/' + products.part1.documentLink +'/A"]';

        return this.waitWhileVisible(modalTitle, function partModalClosed() {
            this.test.assert(true, 'Part modal closed');
        }, function fail() {
            this.capture('screenshot/partClickLink/waitForPartModalClosed-error.png');
            this.test.assert(false, 'Part modal is still displayed');
        });
    });

    /**
     * Wait for linked document modal
     */
    casper.then(function waitForLinkedDocumentModal() {
        var modalTitle = '.document-modal > .modal-header > h3 > a';
        return this.waitForSelector(modalTitle, function linkedDocumentModal() {
            this.test.assertSelectorHasText('.document-modal > .modal-header > h3 > a', products.part1.documentLink , 'Linked document modal opened');
        }, function fail() {
            this.capture('screenshot/partClickLink/waitForLinkedDocumentModal-error.png');
            this.test.assert(false, 'Linked document modal can not be found');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });

});
