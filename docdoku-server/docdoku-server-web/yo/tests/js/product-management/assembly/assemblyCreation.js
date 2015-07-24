/*global casper,urls,products,homeUrl,workspace*/

casper.test.begin('Assembly creation tests suite', 13, function assemblyCreationTestsSuite() {

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
            this.capture('screenshot/assemblyCreation/waitForPartNavLink-error.png');
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
            this.capture('screenshot/assemblyCreation/waitForPartList-error.png');
            this.test.assert(false, 'Part list can not be found');
        });
    });

    /**
     * Wait the modal
     */
    casper.then(function waitForPartModal() {
        var modalTab = '#part-modal .tabs li a[href="#tab-assembly"]';
        this.waitForSelector(modalTab, function modalOpened() {
            this.click(modalTab);
        }, function fail() {
            this.capture('screenshot/assemblyCreation/waitForPartModal-error.png');
            this.test.assert(false, 'Part modal can not be found');
        });
    });

    /**
     * Wait the modal tab
     */
    casper.then(function waitForPartModalTab() {
        this.waitForSelector('#part-modal #tab-assembly .components', function tabSelected() {
            this.test.assert(true, 'Assembly tab opened');
        }, function fail() {
            this.capture('screenshot/assemblyCreation/waitForPartModalTab-error.png');
            this.test.assert(false, 'Part modal tab can not be found');
        });
    });

    /**
     * Fill the form
     */
    var partNumbers = Object.keys(products.assembly.parts);
    var parts = products.assembly.parts;

    partNumbers.forEach(function (partNumber) {
        casper.then(function createNewParts() {

            this.click('#part-modal #create-part-revision-as-part-usage-link');

            var element = '#part-modal #tab-assembly .components .component:last-child';

            // Expand view
            this.click(element + ' .toggle-cad-instances');

            // Fill coordinates
            this.sendKeys(element + ' input[name="number"]', partNumber, {reset: true});
            this.sendKeys(element + ' input[name="tx"]', parts[partNumber].tx.toString(), {reset: true});
            this.sendKeys(element + ' input[name="ty"]', parts[partNumber].ty.toString(), {reset: true});
            this.sendKeys(element + ' input[name="tz"]', parts[partNumber].tz.toString(), {reset: true});
            this.sendKeys(element + ' input[name="rx"]', parts[partNumber].rx.toString(), {reset: true});
            this.sendKeys(element + ' input[name="ry"]', parts[partNumber].ry.toString(), {reset: true});
            this.sendKeys(element + ' input[name="rz"]', parts[partNumber].rz.toString(), {reset: true});
            this.test.assert(true, partNumber + ' created');

        });
    });

    /**
     * Save it
     */
    casper.then(function saveParts() {
        this.click('#part-modal #save-part');
    });

    /**
     * Wait for modal to be closed
     */
    casper.then(function closePartsModal() {
        this.waitWhileSelector('#part-modal', function modalClosed() {
            this.test.assert(true, 'Modal has been closed');
        }, function fail() {
            this.capture('screenshot/assemblyCreation/waitModalToBeClosed-error.png');
            this.test.assert(false, 'Modal is still not closed');
        });
    });

    /**
     * Check that the list contains now 5 parts
     */

    casper.then(function checkIfPartsInAssemblyAreCreated() {
        casper.waitFor(function check() {
            return this.evaluate(function () {
                return document.querySelectorAll('#part_table tbody tr').length === 5;
            });
        }, function then() {
            this.test.assert(true, '5 entries in the table');
        }, function fail() {
            this.capture('screenshot/assemblyCreation/checkIfPartsInAssemblyAreCreated-error.png');
            this.test.assert(false, 'There are not 5 entries in the table');
        });
    });

    /**
     * Check if assembly / leaf icons are well set
     */

    casper.then(function checkAssemblyIconsSet() {
        this.waitForSelector('#part_table .fa.fa-cube', function check() {
            this.test.assertElementCount('#part_table .fa.fa-cube', 4, 'found 4 leaf parts');
            this.test.assertElementCount('#part_table .fa.fa-cubes', 1, 'found 1 assembly part');
        });

    });

    /**
     * Checkin all parts
     */

    partNumbers.forEach(function (partNumber) {
        casper.then(function checkoutPart() {
            // Run xhrs, more convenient here.
            this.open(homeUrl + 'api/workspaces/' + workspace + '/parts/' + partNumber + '-A/checkin', {method: 'PUT'}).then(function (response) {
                this.test.assertEquals(response.status, 200, 'Part ' + partNumber + ' should be checkin');
            }, function () {
                this.test.assert(false, 'Part ' + partNumber + ' has not been checkin');
            });
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });

});
