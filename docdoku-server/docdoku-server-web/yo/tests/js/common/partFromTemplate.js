/*global casper,urls,products,$*/
casper.test.begin('Part from template creation tests suite', 19, function partCreationTestsSuite() {
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
            this.capture('screenshot/partCreation/waitForPartNavLink-error.png');
            this.test.assert(false, 'Part nav link can not be found');
        });
    });

    /**
     * Open the part creation modal
     */
    casper.then(function waitForNewPartButton() {
        this.waitForSelector('.actions .new-part', function clickNewPartButton() {
            this.click('.actions .new-part');
        }, function fail() {
            this.capture('screenshot/parteaCreation/waitForNewPartButton-error.png');
            this.test.assert(false, 'New part button can not be found');
        });
    });

    /**
     * open new modal
     */
    casper.then(function openModal() {
        this.waitForSelector('#part_creation_modal input#inputPartNumber', function onNewPartFormReady() {
            this.test.assert(true, 'modal opened');
        }, function fail() {
            this.capture('screenshot/partCreation/onNewPartFormReady-error.png');
            this.test.assert(false, 'New part form can not be found');
        });
    });

    casper.then(function selectTemplate() {
        //wait for the third option to be loaded, sometimes the modal is created, but the template
        //are still to be injected.
        this.waitForSelector('#inputPartTemplate option:nth-child(3)', function succeed() {
            this.test.assert(true, 'The template are loaded in the creation modal');
            this.evaluate(function () {
                document.querySelector('#inputPartTemplate').selectedIndex = 1;
                $('#inputPartTemplate').change();
                return true;
            });
        }, function fail() {
            this.test.assert(false, 'Could not load the template in the creation modal');
        });
    });
    /**
     * Create a part with its partNumber and its partName and choose a template
     */
    casper.then(function fillNewPartModalForm() {
        this.sendKeys('#part_creation_modal input#inputPartNumber', products.part2.number, {reset: true});
        this.sendKeys('#part_creation_modal input#inputPartName', products.part2.name, {reset: true});
    });


    /**
     * Go to attribute template
     */
    casper.then(function goToAttributeTab() {
        this.click('.nav.nav-tabs > li:nth-child(3) > a');
        this.waitForSelector('.nav.nav-tabs > li:nth-child(3).active', function () {
            this.test.assert(true, 'Attribute tab found');
        }, function () {
            this.capture('screenshot/attributes/clickOnAttributeTab-error.png');
            this.test.assert(false, 'Attribute tab cannot be found');
        });
    });

    casper.then(function countAttributeFromTemplate() {
        this.waitForSelector('#attributes-list .list-item', function () {
            this.test.assertElementCount('#attributes-list input.name[disabled=disabled]', 2,
                'The attributes name input should be disabled');
            this.test.assertElementCount('#attributes-list select.type', 0,
                'The select type should not be present');
            this.test.assertElementCount('#attributes-list div.type', 2,
                'A div with the type of the attribute should be present');
            this.test.assertElementCount('#attributes-list .fa.fa-bars.sortable-handler.invisible', 2,
                'there should be no sortable button');
            this.test.assertElementCount('#attributes-list .fa.fa-times.invisible', 2,
                'there should be no delete button');
        }, function fail() {
            this.test.assert(false, 'list of attributes from template not present');
        });
    });

    /**
     * Go to main tab
     */
    casper.then(function () {
        this.click('.nav.nav-tabs > li:nth-child(1) > a');
        this.waitForSelector('.nav.nav-tabs > li:nth-child(1).active', function () {
            this.test.assert(true, 'Main tab found');

        }, function () {
            this.capture('screenshot/attributes/clickOnMainTab-error.png');
            this.test.assert(false, 'Main tab not appearing');
        });
    });

    /**
     * Choose second template
     */

    casper.then(function fillNewPartModalForm() {
        this.waitForSelector('#part_creation_modal input#inputPartNumber', function onNewPartFormReady() {
            this.test.assertElementCount('#inputPartTemplate option', 3, 'there should be two template available');
            this.evaluate(function () {
                document.querySelector('#inputPartTemplate').selectedIndex = 2;
                $('#inputPartTemplate').change();
                return true;
            });
        }, function fail() {
            this.capture('screenshot/partCreation/onNewPartFormReady-error.png');
            this.test.assert(false, 'New part form can not be found');
        });
    });

    /**
     * Go to attribute template
     */
    casper.then(function () {
        this.click('.nav.nav-tabs > li:nth-child(3) > a');
        this.waitForSelector('.nav.nav-tabs > li:nth-child(3).active', function () {
            this.test.assert(true, 'Attribute tab found');

        }, function () {
            this.capture('screenshot/attributes/clickOnAttributeTab-error.png');
            this.test.assert(false, 'Attribute tab not appearing');
        });
    });

    /**
     * Assert attributes input are present and rightly displayed.
     */
    casper.then(function countAttributeFromTemplate() {

        this.waitForSelector('#attributes-list .list-item', function () {
            this.test.assertElementCount('#attributes-list input.name[disabled=disabled]', 1,
                'The attributes name input should be disabled');
            this.test.assertElementCount('#attributes-list select.type', 1,
                'Only one select type should be present');
            this.test.assertElementCount('#attributes-list div.type', 1,
                'A div with the type of the attribute should be present');
            this.test.assertElementCount('#attributes-list input.value[required]', 1,
                'One input Value should be required (mandatory)');
            this.test.assertElementCount('#attributes-list .fa.fa-bars.sortable-handler.invisible', 0,
                'there should be sortable button');
            this.test.assertElementCount('#attributes-list .fa.fa-times.invisible', 1,
                'there should be one delete button invisible');
            this.test.assertElementCount('#attributes-list .fa.fa-times', 2,
                'there should be two delete button');
        }, function fail() {
            this.test.assert(false, 'list of attributes from template not present');
        });
    });

    /**
     * Dismiss and close modal
     */
    casper.then(function waitForModalToBeClosed() {
        this.click('.btn[data-dismiss="modal"]');
        this.waitWhileSelector('#part_creation_modal', function onPartModalClosed() {
            this.test.assert(true, 'Part modal has been closed');
        }, function fail() {
            this.capture('screenshot/partCreation/waitForModalToBeClosed-error.png');
            this.test.assert(false, 'Part modal can not close');
        });
    });


    casper.run(function allDone() {
        this.test.done();
    });

});
