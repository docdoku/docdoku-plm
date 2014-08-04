/*global casper,__utils__,workspaceUrl,partCreationNumber*/
'use strict';
casper.test.begin('Delete a part is available',1, function(){
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
     * Test delete a part
     */
    casper.waitForSelector('#part_table tbody tr', function() {
        var goodProduct = this.evaluate(function(partNumber){
            return $('#part_table').find('tbody tr:first-child td:nth-child(2) .part_number_value').html()===partNumber;
        },partCreationNumber);
        if(!goodProduct){
            this.test.fail('Created part not found');
            this.exit('Created part not found');
        }
        this.evaluate(function(){__utils__.log('Created part found', 'info');});

        // Search the created part checkbox
        exists = this.evaluate(function() {
            return __utils__.exists('#part_table tbody tr:first-child td:first-child input');
        });
        if(!exists){
            this.test.fail('Checkbox not found');
            this.exit('Checkbox not found');
        }
        this.evaluate(function(){__utils__.log('Checkbox found', 'info');});
        this.click('#part_table tbody tr:first-child td:first-child input');

        // Delete the part
        this.wait(500, function(){
            exists = this.evaluate(function() {
                return __utils__.exists('.delete');
            });
            if(!exists){
                this.test.fail('Delete part button not found');
                this.exit('Delete part button not found');
            }
            this.evaluate(function(){__utils__.log('Delete part button found', 'info');});
            this.click('.delete');
        });
        // Popup handled auto.
        // Check if the created product still here
        this.wait(1000, function(){
            this.test.assertDoesntExist('#part tbody tr:first-child td:nth-child(2)','Created part should be deleted');
        });
    });

    casper.run(function() {
        this.test.done();
    });
});