/*global casper,urls,products*/

casper.test.begin('Assembly check tests suite', 29, function assemblyCheckTestsSuite() {

    'use strict';

    casper.open('');

    /**
     * Open product management URL
     * */

    casper.then(function () {
        this.open(urls.productManagement);
    });

    /**
     * Go to part nav
     */
    casper.then(function waitForPartNavLink() {
        this.waitForSelector('#part-nav > .nav-list-entry > a', function clickPartNavLink() {
            this.click('#part-nav > .nav-list-entry > a');
        }, function fail() {
            this.capture('screenshot/assemblyCheck/waitForPartNavLink-error.png');
            this.test.assert(false, 'Part nav link can not be found');
        });
    });

    /**
     * Wait the part list
     */
    casper.then(function waitForPartList() {
        var link = '#part_table tbody tr:first-child td.part_number';
        this.waitForSelector(link, function clickPartNavLink() {
            this.click(link);
        }, function fail() {
            this.capture('screenshot/assemblyCheck/waitForPartList-error.png');
            this.test.assert(false, 'Part list can not be found');
        });
    });

    /**
     * Wait the modal
     */
    casper.then(function waitForPartModal() {
        var modalTab = '#part-modal .tabs li a[href="#tab-part-sub-parts"]';
        this.waitForSelector(modalTab, function modalOpened() {
            this.click(modalTab);
        }, function fail() {
            this.capture('screenshot/assemblyCheck/waitForPartModal-error.png');
            this.test.assert(false, 'Part modal can not be found');
        });
    });

    /**
     * Wait the modal tab
     */
    casper.then(function waitForPartModalTab() {
        this.waitForSelector('#part-modal .component', function tabSelected() {
            this.test.assert(true, 'Assembly tab opened');
        }, function fail() {
            this.capture('screenshot/assemblyCheck/waitForPartModalTab-error.png');
            this.test.assert(false, 'Part modal tab can not be found');
        });
    });

    /**
     * Check all partNumbers coordinates
     */

    /**
     * Fill the form
     */
    var partNumbers = Object.keys(products.assembly.parts);
    var parts = products.assembly.parts;

    partNumbers.forEach(function (partNumber) {
        casper.then(function checkUsageLink() {

            this.test.assertExists('.component[data-part-number="' + partNumber + '"]', 'Part ' + partNumber + ' is in the assembly');

            this.test.assertExists('.component[data-part-number="' + partNumber + '"] .coord[name="tx"][value="' + parts[partNumber].tx + '"]', 'Tx OK');
            this.test.assertExists('.component[data-part-number="' + partNumber + '"] .coord[name="ty"][value="' + parts[partNumber].ty + '"]', 'Ty OK');
            this.test.assertExists('.component[data-part-number="' + partNumber + '"] .coord[name="tz"][value="' + parts[partNumber].tz + '"]', 'Tz OK');

            this.test.assertExists('.component[data-part-number="' + partNumber + '"] .coord[name="rx"][value="' + parts[partNumber].rx + '"]', 'Rx OK');
            this.test.assertExists('.component[data-part-number="' + partNumber + '"] .coord[name="ry"][value="' + parts[partNumber].ry + '"]', 'Ry OK');
            this.test.assertExists('.component[data-part-number="' + partNumber + '"] .coord[name="rz"][value="' + parts[partNumber].rz + '"]', 'Rz OK');

        });
    });


    casper.run(function allDone() {
        this.test.done();
    });

});
