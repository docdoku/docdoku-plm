/*global casper, workspaceUrl,partCreationNumber,partCreationName,__utils__*/
'use strict';
casper.test.begin('Show a part details is available',4, function(){
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

    var partName;
    var partNumber;
    /**
     * Find the created part in the list
     */
    casper.waitForSelector('#part_table tbody tr',function showPartDetails() {
        partName = this.getHTML('#part_table tbody tr:first-child td:nth-child(6)');                                    // Todo dont look at the first child
        partNumber = this.getHTML('#part_table tbody tr:first-child .part_number .part_number_value');
        this.test.assertEquals(partNumber, partCreationNumber, 'Created part number should be valid in List');
        this.test.assertEquals(partName, partCreationName, 'Created part name should be valid in List');
        exists = this.evaluate(function() {
            return __utils__.exists('#part_table tbody tr:first-child .part_number');
        });
        if(!exists){
            this.test.fail('Part modal link not found');
            this.exit('Part modal link not found');
        }
        this.evaluate(function(){__utils__.log('Part modal link found', 'info');});
        this.click('#part_table tbody tr:first-child .part_number');
    });

    /**
     * Test content of part modal
     */
    casper.waitForSelector('#part-modal',function testPartModal() {
        this.test.assertEquals(partNumber, this.getHTML('#form-part div:first-child div span'), 'Created part number should be valid in modal');
        this.test.assertEquals(partName, this.getHTML('#form-part div:nth-child(2) div span'), 'Created part name should be valid in modal');
    });

    /**
     * Close modal
     */
    casper.then(function () {
        casper.then(function closeModal() {
            exists = this.evaluate(function() {
                return __utils__.exists('#cancel-iteration');
            });
            if(!exists){
                this.test.fail('Cancel button not found');
                this.exit('Cancel button not found');
            }
            this.evaluate(function(){__utils__.log('Cancel button found', 'info');});
            this.click('#cancel-iteration');
        });
    });

    // screenshot
    /*casper.then(function() {
     this.wait(1000, function(){
     this.capture('screenshot/part.png');
     });
     });*/

    casper.run(function() {
        this.test.done();
    });
});