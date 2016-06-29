/*global casper,urls,products*/

casper.test.begin('Part template attributes tests suite', 14, function partTemplateCreationTestsSuite() {
    'use strict';

    casper.open('');

    /**
     * Open product management URL
     * */

    casper.then(function () {
        return this.open(urls.productManagement);
    });

    casper.then(function waitForPartTemplateNavLink() {
        return this.waitForSelector('#part-template-nav > .nav-list-entry > a', function clickPartTemplateNavLink() {
            this.click('#part-template-nav > .nav-list-entry > a');
        }, function fail() {
            this.capture('screenshot/partTemplateCreation/waitForPartTemplateNavLink-error.png');
            this.test.assert(false, 'Part template nav link can not be found');
        });
    });

    /**
     * Edit the first template
     */
    casper.then(function waitForPartTemplateContent() {
        //should select the one created
        return this.waitForSelector('#part_template_table tbody tr:first-child td.reference', function clickPartTemplateCreationButton() {
            this.click('#part_template_table tbody tr:first-child td.reference');
        }, function fail() {
            this.capture('screenshot/partTemplateCreation/waitForPartTemplateCreationButton-error.png');
            this.test.assert(false, 'Part template creation button can not be found');
        });
    });

    /**
     * open attribute tab
     */
    casper.then(function () {
        var attributesTabSelector = '.nav.nav-tabs > li:nth-child(3) > a';
        return this.waitForSelector(attributesTabSelector, function openTab() {
            this.click(attributesTabSelector);
        }, function fail() {
            this.capture('screenshot/attributes/clickOnAttributeTab-error.png');
            this.test.assert(false, 'Attribute tab cannot be found');
        });
    });

    /**
     * wait for tab to be active
     */
    casper.then(function assertTabActive() {
        return this.waitForSelector('.nav.nav-tabs > li:nth-child(3).active', function () {
            this.test.assert(true, 'attributes tab active');
        }, function () {
            this.capture('screenshot/attributes/attributeTabBecomeActive-error.png');
            this.test.assert(false, 'Attribute tab not appearing');
        });
    });

    /**
     * add new attributes
     */
    casper.then(function addNewAttributes() {
        var addAttributeButtonSelector = '#attributes-list .btn.add';
        return this.waitForSelector(addAttributeButtonSelector, function () {
            this.click(addAttributeButtonSelector);
        }, function () {
            this.capture('screenshot/attributes/attributeTabBecomeActive-error.png');
            this.test.assert(false, 'Add attribute button not found');
        });
    });

    /**
     * Test the lock/freeze/mandatory checkbox present
     */
    casper.then(function testLockAttributePresent() {
        return this.waitForSelector('.lock', function () {
            this.test.assert(true, 'lock checkbox present');
            this.test.assertExists('.attribute-locked', 'attribute-locked present');
            this.test.assertExists('.attribute-mandatory', 'attribute-mandatory present');
        }, function fail() {
            this.capture('screenshot/attributes/all-lock-not-found.png');
            this.test.assert(false, 'the lock all checkbox should exist');
        });
    });

    /**
     * Clicking on freeze (.lock) should remove the attribute-locked checkbox
     */
    casper.then(function testAttrLockRemoved() {
        this.click('.lock');
        return this.waitWhileSelector('.attribute-locked', function removingAttrLock() {
            this.test.assert(true, 'attribute-locked removed');
            this.test.assertExists('.attribute-mandatory', 'attribute-mandatory present');
        }, function fail() {
            this.capture('screenshot/attributes/AttributeLockNotRemoved.png');
            this.test.assert(false, 'attribute-locked should have been removed');
        });

    });

    /**
     * when freeze is not checked, the attribute-locked checkbox should appear
     */
    casper.then(function testAttrLockPresent() {
        this.click('.lock');
        return this.waitForSelector('.attribute-locked', function () {
            this.test.assert(true, 'attribute-locked exist');
        }, function fail() {
            this.test.assert(false, 'attribute-locked should exist');
        });
    });

    /**
     * Creation of a template with all attributes lock
     */
    casper.then(function () {
        this.click('.lock');
        this.click('#attributes-list .btn.add');
        return this.waitForSelector('.list-item.well:nth-child(2)', function () {
            this.test.assertElementCount('.list-item.well', 2);
            this.sendKeys('#attributes-list .list-item:nth-child(1) input.name', products.part2.attributeName1, {reset: true});
            this.sendKeys('#attributes-list .list-item:nth-child(2) input.name', products.part2.attributeName1, {reset: true});
            this.click('#part_template_creation_modal .btn.btn-primary');
        }, function () {
            this.capture('screenshot/attributes/addAttribute-error.png');
            this.test.assert(false, 'Attribute not appearing in the list');
        });

    });

    casper.then(function closeModal() {
        return this.waitWhileSelector('#part_template_creation_modal', function () {
            this.test.assert(true, 'modal closed');
        }, function fail() {
            this.capture('screenshot/attributes/closeModal-error.png');
            this.test.assert(false, 'could not close the modal');
        });
    });

    /**
     * Check if the template has been created and open the edit modal
     */
    casper.then(function openEditModal() {
        return this.waitForSelector('#part_template_table tbody tr:first-child td.reference', function clickPartTemplateCreationButton() {
            this.click('#part_template_table tbody tr:first-child td.reference');

        }, function fail() {
            this.capture('screenshot/partTemplateCreation/waitForPartTemplateCreationButton-error.png');
            this.test.assert(false, 'Part template creation button can not be found');
        });
    });

    /**
     * open attribute tab
     */
    casper.then(function openAttrTab() {
        var attributesTabSelector = '.nav.nav-tabs > li:nth-child(3) > a';
        return this.waitForSelector(attributesTabSelector, function openTab() {
            this.click(attributesTabSelector);
        }, function fail() {
            this.capture('screenshot/attributes/clickOnAttributeTab-error.png');
            this.test.assert(false, 'Attribute tab cannot be found');
        });
    });


    casper.then(function () {
        return this.waitForSelector('#attributes-list .list-item.well:nth-child(2)', function () {
            this.test.assertElementCount('#attributes-list .list-item.well', 2);
        }, function fail() {
            this.capture('screenshot/partTemplateCreation/waitForAttrTab-error.png');
            this.test.assert(false, 'could not open modal for edition');
        });
    });

    /**
     * close modal
     */
    casper.then(function () {
        this.click('.btn[data-dismiss="modal"]');
        return this.waitWhileSelector('#part_template_creation_modal', function () {
                this.test.assert(true, 'modal closed');
            },
            function fai() {
                this.capture('screenshot/attributes/CloseModal-error.png');
                this.test.assert(false, 'could not close modal');
            });
    });

    /**
     * Wait for the creation button
     */
    casper.then(function waitForPartTemplateCreationButton() {
        return this.waitForSelector('.actions .new-template', function clickPartTemplateCreationButton() {
            this.click('.actions .new-template');
        }, function fail() {
            this.capture('screenshot/partTemplateCreation/waitForPartTemplateCreationButton-error.png');
            this.test.assert(false, 'Part template creation button can not be found');
        });
    });


    /**
     * Fill template form
     */
    casper.then(function fillTheForm() {
        return this.waitForSelector('#part_template_creation_modal', function modalOpened() {
            this.sendKeys('#part_template_creation_modal #part-template-reference', products.template2.number, {reset: true});
            this.sendKeys('#part_template_creation_modal #part-template-type', products.template2.type, {reset: true});
        }, function fail() {
            this.capture('screenshot/partTemplateCreation/waitForPartTemplateCreationModal-error.png');
            this.test.assert(false, 'Part template creation modal not found');
        });


    });

    /**
     * Go to the attributes tab
     */
    casper.then(function goToAttributesTab() {
        this.click('.nav.nav-tabs > li:nth-child(3) > a');
        return this.waitForSelector('.nav.nav-tabs > li:nth-child(3).active', function () {
            this.test.assert(true, 'Attribute Tab active');
        }, function fail() {
            this.capture('screenshot/attributes/attributeTabBecomeActive-error.png');
            this.test.assert(false, 'Attribute tab not appearing');
        });
    });

    casper.then(function addAttributes() {
        var addAttributeButtonSelector = '#attributes-list .btn.add';
        var addInstanceAttributeButtonSelector = '#attribute-product-instance-list .btn.add';
        return this.waitForSelector(addAttributeButtonSelector, function () {
            this.click(addAttributeButtonSelector);
            this.click(addAttributeButtonSelector);
            //add an instance attribute
            this.click(addInstanceAttributeButtonSelector);
        }, function () {
            this.capture('screenshot/attributes/addButtonNotAppearing.png');
            this.test.assert(false, 'Add attribute button not found');
        });
    });

    /**
     * fill the attribute of the template
     */
    casper.then(function fillTemplateAttributes() {

        return this.waitForSelector('#attribute-product-instance-list .list-item.well', function () {
            this.test.assertElementCount('.list-item.well', 3, '3 elements should have been created, 2 attributes and' +
            ' 1 instance attribute');
            this.sendKeys('#attributes-list .list-item:nth-child(1) input.name', products.part2.attributeName1, {reset: true});
            this.sendKeys('#attributes-list .list-item:nth-child(2) input.name', products.part2.attributeName2, {reset: true});
            this.sendKeys('#attribute-product-instance-list .list-item input.name', products.template2.attrInstance, {reset: true});
            this.click('#attributes-list .list-item:nth-child(2) .checkbox.attribute-locked');
            this.click('#attributes-list .list-item:nth-child(2) .checkbox.attribute-mandatory');
            this.click('#attribute-product-instance-list .list-item.well .checkbox.attribute-locked');
            this.click('#attribute-product-instance-list .list-item.well .checkbox.attribute-mandatory');
            this.click('#part_template_creation_modal .btn.btn-primary');
        }, function () {
            this.capture('screenshot/attributes/addAttribute-error.png');
            this.test.assert(false, 'Attribute not appearing in the list');
        });

    });


    casper.then(function saveTemplate() {
        return this.waitWhileSelector('#part_template_creation_modal', function () {
            this.test.assert(true, 'modal closed');
        }, function fail() {
            this.test.assert(false, 'could not close the modal');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });
});
