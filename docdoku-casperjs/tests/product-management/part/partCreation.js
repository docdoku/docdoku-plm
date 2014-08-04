/*global casper,__utils__, partCreationNumber, partCreationName, deletePartUrl, workspaceUrl*/
'use strict';
casper.test.begin('Create a part is available',2, function(){
    casper.thenOpen(workspaceUrl);
    var exists;

    /**
     * Go to product management
     */
    casper.then(function openProductManagement() {
        exists = this.evaluate(function() {
            return __utils__.exists('#products_management_link a');
        });
        if(!exists){
            this.test.fail('Products management link not found');
            this.exit('Products management link not found');
        }
        this.evaluate(function(){__utils__.log('Products management link found', 'info');});
        this.click('#products_management_link a');
    });

    /**
     * Go to part nav
     */
    casper.waitForSelector('#part-nav',function openPartNav() {
        exists = this.evaluate(function() {
            return __utils__.exists('#part-nav div a');
        });
        if(!exists){
            this.test.fail('Parts link not found');
            this.exit('Parts link not found');
        }
        this.evaluate(function(){__utils__.log('Parts link found', 'info');});
        this.click('#part-nav div a');
    });

    /**
     * Open the part creation modal
     */
    casper.waitForSelector('.new-part',function openPartCreationModal() {
        exists = this.evaluate(function() {
            return __utils__.exists('.new-part');
        });
        if(!exists){
            this.test.fail('New part button not found');
            this.exit('New part button not found');
        }
        this.evaluate(function(){__utils__.log('New part button found', 'info');});
        this.click('.new-part');
    });

    /**
     * Test create a part without fill the input
     */
    casper.then(function createPartWithEmptyField() {
        this.wait(1000, function (){
            exists = this.evaluate(function() {
                return __utils__.exists('#part_creation_modal .btn-primary');
            });
            if(!exists){
                this.test.fail('New part submit button not found');
                this.exit('New part submit button not found');
            }
            this.evaluate(function(){__utils__.log('New part submit button found', 'info');});
            this.click('#part_creation_modal .btn-primary');
        });
        this.wait(1000, function (){
            this.test.assertExists('#part_creation_modal .btn-primary', 'Should not create a empty part');
        });
    });

    /**
     * Test create a part with its partNumber and its partName
     */
    casper.then(function createPartWithFilledField() {
            this.wait(1000, function (){
                // Search Part Number Input
                exists = this.evaluate(function() {
                    return __utils__.exists('#inputPartNumber');
                });
                if(!exists){
                    this.test.fail('Part number input not found');
                    this.exit('Part number input not found');
                }
                this.evaluate(function(){__utils__.log('Part number input found', 'info');});
                this.sendKeys('#inputPartNumber', partCreationNumber, {reset:true});

                // Search Part Name Input
                exists = this.evaluate(function() {
                    return __utils__.exists('#inputPartName');
                });
                if(!exists){
                    this.test.fail('Part number name not found');
                    this.exit('Part number name not found');
                }
                this.evaluate(function(){__utils__.log('Part name input found', 'info');});
                this.sendKeys('#inputPartName', partCreationName, {reset:true});

                // Search Part Creation Submit Button
                exists = this.evaluate(function() {
                    return __utils__.exists('#part_creation_modal .btn-primary');
                });
                if(!exists){
                    this.test.fail('Part creation submit button disappear');
                    this.exit('Part creation submit button disappear');
                }
                this.evaluate(function(){__utils__.log('Part creation submit button still found', 'info');});
                this.click('#part_creation_modal .btn-primary');
            });
            this.wait(1000, function (){
                this.test.assertDoesntExist('#part_creation_modal .btn-primary', 'Should creating the part '+partCreationName);
            });
    });

    // screenshot
    /*casper.then(function() {
        this.wait(1000, function(){
            this.capture('screenshot/partCreation.png');
        });
    });*/

    casper.run(function() {
        this.test.done();
    });
});