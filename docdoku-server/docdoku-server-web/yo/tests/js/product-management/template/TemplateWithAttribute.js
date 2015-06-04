/**
 * Created by kelto on 02/06/15.
 */

casper.test.begin('Part template attributes tests suite', 16, function partTemplateCreationTestsSuite() {
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
     * Edit the first template
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
    /**
     * Creation of a template with all attributes lock
     */
    casper.then(function () {
        this.click('.lock');
        this.click('.btn.add');
        this.waitForSelector('.list-item.well:nth-child(2)', function () {
            this.test.assertElementCount('.list-item.well', 2);
            this.sendKeys('#attributes-list .list-item:nth-child(1) input.name', products.part2.attributeName1, {reset:true});
            this.sendKeys('#attributes-list .list-item:nth-child(2) input.name', products.part2.attributeName1, {reset:true});
            this.click('.btn.btn-primary');
        }, function () {
            this.capture('screenshot/attributes/addAttribute-error.png');
            this.test.assert(false, 'Attribute not appearing in the list');
        });
        this.waitWhileSelector('#part_template_creation_modal', function() {
            this.test.assert(true, 'modal closed');
        }, function fail() {
            this.test.assert(false, 'could not close the modal');
        });
    });

    /**
     * Check if the template has been created and if the attributes are saved
     */
    casper.then(function() {
        this.waitForSelector('#part_template_table tbody tr:first-child td.reference',function clickPartTemplateCreationButton() {
            this.click('#part_template_table tbody tr:first-child td.reference');
            this.waitForSelector('#attributes-list', function() {
                this.test.assertElementCount('#attributes-list .list-item.well', 2);
            }, function fail() {
                this.capture('screenshot/partTemplateCreation/waitForAttrTab-error.png');
                this.test.assert(false, 'could not open modal for edition');
            });
        },function fail(){
            this.capture('screenshot/partTemplateCreation/waitForPartTemplateCreationButton-error.png');
            this.test.assert(false,'Part template creation button can not be found');
        });
    });

    /**
     * close modal
     */
    casper.then(function() {
        this.click('.btn[data-dismiss="modal"]');
        this.waitWhileSelector('#part_template_creation_modal', function() {
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
    casper.then(function waitForPartTemplateCreationButton(){
        this.waitForSelector('.actions .new-template',function clickPartTemplateCreationButton() {
            this.click('.actions .new-template');
        },function fail(){
            this.capture('screenshot/partTemplateCreation/waitForPartTemplateCreationButton-error.png');
            this.test.assert(false,'Part template creation button can not be found');
        });
    });




    /**
     * Fill template form
     */
    casper.then(function fillTheForm(){
        this.waitForSelector('#part_template_creation_modal',function modalOpened() {
            this.sendKeys('#part_template_creation_modal #part-template-reference',products.template2.number,{reset:true});
            this.sendKeys('#part_template_creation_modal #part-template-type',products.template2.type,{reset:true});
        },function fail(){
            this.capture('screenshot/partTemplateCreation/waitForPartTemplateCreationModal-error.png');
            this.test.assert(false,'Part template creation modal not found');
        });


    });

    /**
     * Go to the attributes tab
     */
    casper.then(function goToAttributesTab() {
        var attributesTabSelector = '.nav.nav-tabs > li:nth-child(3) > a';
        this.waitForSelector(attributesTabSelector, function () {
            this.click(attributesTabSelector);
            this.waitForSelector('.nav.nav-tabs > li:nth-child(3).active', function () {
                this.test.assert(true, 'Attribute Tab active');
            }, function fail() {
                this.capture('screenshot/attributes/attributeTabBecomeActive-error.png');
                this.test.assert(false, 'Attribute tab not appearing');
            });
        }, function () {
            this.capture('screenshot/attributes/clickOnAttributeTab-error.png');
            this.test.assert(false, 'Attribute tab cannot be found');
        });
    });

    casper.then(function addAttributes() {
        var addAttributeButtonSelector = '#attributes-list .btn.add';
        var addInstanceAttributeButtonSelector = '#attribute-product-instance-list .btn.add';
        this.waitForSelector(addAttributeButtonSelector, function () {
            this.click(addAttributeButtonSelector);
            this.click(addAttributeButtonSelector);
            //add an instance attribute
            this.click(addInstanceAttributeButtonSelector);
        }, function () {
            this.capture('screenshot/attributes/addButtonNotAppearing.png');
            this.test.assert(false, 'Add attribute button not found');
        });
    })

    /**
     * fill the attribute of the template
     */
    casper.then(function fillTemplateAttributes() {

        this.waitForSelector('#attribute-product-instance-list .list-item.well', function () {
            this.test.assertElementCount('.list-item.well', 3, '3 elements should have been created, 2 attributes and' +
                ' 1 instance attribute');
            this.sendKeys('#attributes-list .list-item:nth-child(1) input.name', products.part2.attributeName1, {reset:true});
            this.sendKeys('#attributes-list .list-item:nth-child(2) input.name', products.part2.attributeName2, {reset:true});
            this.sendKeys('#attribute-product-instance-list .list-item input.name', products.template2.attrInstance, {reset:true});
            this.click('#attributes-list .list-item:nth-child(2) .checkbox.attribute-locked');
            this.click('#attributes-list .list-item:nth-child(2) .checkbox.attribute-mandatory');
            this.click('#attribute-product-instance-list .list-item.well .checkbox.attribute-locked');
            this.click('#attribute-product-instance-list .list-item.well .checkbox.attribute-mandatory');
            this.click('.btn.btn-primary');
        }, function () {
            this.capture('screenshot/attributes/addAttribute-error.png');
            this.test.assert(false, 'Attribute not appearing in the list');
        });

    });


    casper.then(function saveTemplate() {
        this.waitWhileSelector('#part_template_creation_modal', function() {
            this.test.assert(true, 'modal closed');
        }, function fail() {
            this.test.assert(false, 'could not close the modal');
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});
