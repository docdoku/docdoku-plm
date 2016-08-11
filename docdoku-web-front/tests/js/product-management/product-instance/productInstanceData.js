/*global casper,urls,productInstances,$*/

casper.test.begin('Product instance data path tests suite', 22, function productInstanceDataPathTestsSuite() {
    'use strict';

    casper.open('');

    /**
     * Open product management URL
     * */

    casper.then(function () {
        return this.open(urls.productStructureForDeliverable);
    });


    /**
     * Wait for Serial Number selected
     */
    casper.then(function waitForSerialNumber() {
        return this.waitForSelector('#config_spec_type_selector_list', function loadConfiguration() {
            return this.evaluate(function () {
                var selectedIndex = document.querySelector('#config_spec_type_selector_list').selectedIndex;
                return selectedIndex === 2;
            }, function then() {
                this.test.assert(true, 'Serial number config selected');
            }, function fail() {
                this.capture('screenshot/product-instance/SerialNumberConfigNotYetSelected.png');
                this.test.assert(false, 'Serial number config is not selected');
            });
        });
    });

    /**
     * Click on the checkbox from the bom
     */
    casper.then(function waitForBOM() {
        return this.waitForSelector('.selectable-part-checkbox', function loadDataButton() {
            this.click('.selectable-part-checkbox');
            this.test.assert(true, 'click on product-instance checkbox');
        }, function fail() {
            this.capture('screenshot/product-instance/FailToLoadDeliverable.png');
            this.test.assert(false, 'could not load deliverable');
        });
    });

    /**
     * Wait for the Deliverable Data button
     */
    casper.then(function waitForDeliverableButton() {
        return this.waitUntilVisible('#path_data_btn', function clickOnDeliverableButton() {
            this.test.assert(true, 'deliverable data button present');
            this.click('#path_data_btn');
        }, function fail() {
            this.capture('screenshot/product-instance/NoDeliverableButton.png');
            this.test.assert(false, 'deliverable data button not present');
        });

    });

    /**
     * Wait for the Deliverable Data modal
     */
    casper.then(function openDataModal() {
        return this.waitForSelector('.product-instance-data-modal', function waitForModal() {
            this.test.assert(true, 'modal opened');
        }, function fail() {
            this.capture('screenshot/product-instance/ModalNotFound.png');
            this.test.assert(false, 'could not open modal');
        });
    });

    /**
     * Count the part attributes present in the modal
     */
    casper.then(function countPartAttribute() {
        return this.waitForSelector('#partAttributes .list-item', function countAttributes() {
            this.test.assertElementCount('#partAttributes .list-item', 2, '2 parts attributes present');
        }, function fail() {
            this.capture('screenshot/product-instance/CouldNotLoadPartAttributes.png');
            this.test.assert(false, 'could not load the part attributes');
        });

    });

    casper.then(function countPathDataAttr() {
        return this.waitForSelector('#pathDataAttributes', function countDataPath() {
            this.test.assertElementCount('#pathDataAttributes .list-item', 1, '1 data path attr present');
        }, function fail() {
            this.test.assert(false, 'could not load the data path attributes');
        });
    });

    /**
     * Count tab present in the modal
     */
    casper.then(function countTabPresent() {
        this.test.assertElementCount('ul.nav.nav-tabs li', 2, '2 tabs present in the modal');
    });

    /**
     * Add iteration note and save
     */
    casper.then(function addIterationNote() {
        this.sendKeys('.description-input', productInstances.productInstance1.iterationNote);
        this.click('.save-button');
        this.test.assertExists('#pathDataAttributes input.value:invalid',
            'should not create iteration without the mandatory attribute');
        this.sendKeys('#pathDataAttributes input.value', productInstances.productInstance1.pathDataValue);
        this.click('.save-button');
    });

    /**
     * close the modal
     */
    casper.then(function reopenModal() {
        return this.waitWhileSelector('.product-instance-data-modal', function waitCloseModal() {
            this.test.assert(true, 'modal closed');
        }, function fail() {
            this.capture('screenshot/product-instance/ModalNotClosing.png');
            this.test.assert(false, 'could not close the modal');
        });
    });

    /**
     * Check for icon in PS
     */
    casper.then(function waitForPathDataIcon() {
        return this.waitForSelector('#product_nav_list_container > .treeview > ul > li >  i.fa-asterisk', function iconShown() {
            this.test.assert(true, 'Should refresh the treeview and show the path data icon');
        }, function fail() {
            this.capture('screenshot/product-instance/ModalNotClosing.png');
            this.test.assert(false, 'could not close the modal');
        });
    });

    /**
    * click on the checkbox from the bom
    */
    casper.then(function waitForBOM() {
        return this.waitForSelector('.selectable-part-checkbox', function loadDataButton() {
            this.click('.selectable-part-checkbox');
            this.test.assert(true, 'click on product-instance checkbox');
        }, function fail() {
            this.capture('screenshot/product-instance/FailToLoadDeliverable.png');
            this.test.assert(false, 'could not load deliverable');
        });
    });

    /**
     * re-open the modal
     */
    casper.then(function reopenModal() {
        return this.waitForSelector('#path_data_btn', function openModal() {
            this.click('#path_data_btn');
            this.test.assert(true, 'deliverable data button present');
        }, function fail() {
            this.capture('screenshot/product-instance/NoDeliverableButton.png');
            this.test.assert(false, 'deliverable data button not present');
        });
    });

    casper.then(function waitForModal() {
        return this.waitForSelector('.product-instance-data-modal', function modalOpened() {
            this.test.assert(true, 'deliverable data modal opened');
        }, function fail() {
            this.capture('screenshot/product-instance/NoDeliverableModal.png');
            this.test.assert(false, 'could not open deliverable data modal');
        });
    });

    /**
     * Now there should be 4 tab present and an iteration note.
     */
    casper.then(function countTab() {
        return this.waitForSelector('#tab-attributes', function waitForModal() {
            this.test.assertElementCount('ul.nav.nav-tabs li', 4, '2 tabs present in the modal');
            this.test.assertExists('.product-instance-data-modal div.path-description');
            this.test.assertExists('input.description-input[value="' + productInstances.productInstance1.iterationNote + '"]');

        }, function fail() {
            this.capture('screenshot/product-instance/DeliverableDataModal-notFound.png');
            this.test.assert(false, 'deliverable data modal not found');
        });

    });

    /**
     * Test if the iteration note is present
     */
    casper.then(function assertIterationNote() {
        //Wait for the input value to be injected, can take some time.
        return this.waitForSelector('#pathDataAttributes input.value[value="' + productInstances.productInstance1.pathDataValue + '"]', function found() {
            this.test.assert(true, 'the input value is given to the view');
        }, function fail() {
            this.test.assert(false, 'the previously given value is not printed in the input');
        });
    });


    /**
     * Go to the tab attributes
     */
    casper.then(function waitForModal() {
        return this.waitForSelector('.product-instance-data-modal #tab-attributes', function waitModal() {
            this.click('ul.nav.nav-tabs li:nth-child(2) a');
        }, function fail() {
            this.test.assert(false, 'could not open modal');
        });
    });

    /**
     * Test the attributes
     */
    casper.then(function testDataAttributes() {
        return this.waitForSelector('ul.nav.nav-tabs li:nth-child(2).active', function () {
            this.test.assertElementCount('#partAttributes input.name[disabled]', 2,
                'the two part attributes name should be disabled in partAttributes');
            this.test.assertDoesntExist('#partAttributes input.value',
                'There should be no input for value in partAttributes');
            this.test.assertElementCount('#partAttributes div.controls.type', 2,
                'the type should be disabled for partAttributes');
            this.test.assertElementCount('#pathDataAttributes input.name[disabled]', 1,
                'the two part attributes name should be disabled for the pathDataAttributes');
            this.test.assertElementCount('#pathDataAttributes div.controls.type', 1,
                'the type should be disabled for pathDataAttributes');
            this.test.assertElementCount('#pathDataAttributes input.value[required]', 1,
                'the path data input for value should be required');
            this.click('.cancel-button');
        }, function fail() {
            this.capture('screenshot/product-instance/DeliverableTabAttributes-NotFound.png');
            this.test.assert(false, 'could not load the attribute tab');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });
});
