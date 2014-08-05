/*global casper,workspaceUrl,__utils__,productCreationNumber,partCreationNumber*/
'use strict';
casper.test.begin('Delete a product is available',1, function(){
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
     * Go to product nav
     */
    casper.waitForSelector('#part-nav',function openProductNav() {
        exists = this.evaluate(function() {
            return __utils__.exists('#product-nav div a');
        });
        if(!exists){
            this.test.fail('Products link not found');
            this.exit('Products link not found');
        }
        this.evaluate(function(){__utils__.log('Products link found', 'info');});
        this.click('#product-nav div a');
    });

    /**
     * Test delete a product
     */
    casper.waitForSelector('#product_table tbody tr:first-child',function deleteProduct() {
        // Search the created product
        var goodProduct = this.evaluate(function (productNumber, partNumber) {
            var $productTable = $('#product_table');
            var bool = $productTable.find('tbody tr:first-child td:nth-child(3)').html() === partNumber;
            return bool && $productTable.find('tbody tr:first-child td:nth-child(2)').html() === productNumber;
        }, productCreationNumber, partCreationNumber);
        if (!goodProduct) {
            this.test.fail('Created product not found');
            this.exit('Created product not found');
        }
        this.evaluate(function () {
            __utils__.log('Created product found', 'info');
        });

        // Search the created product checkbox
        exists = this.evaluate(function () {
            return __utils__.exists('#product_table tbody tr:first-child td:first-child input');
        });
        if (!exists) {
            this.test.fail('Checkbox not found');
            this.exit('Checkbox not found');
        }
        this.evaluate(function () {
            __utils__.log('Checkbox found', 'info');
        });
    });
    casper.thenClick('#product_table tbody tr:first-child td:first-child input',function(){
        // Delete the product
        this.wait(500, function() {
            exists = this.evaluate(function () {
                return __utils__.exists('.delete');
            });
            if (!exists) {
                this.test.fail('Delete product button not found');
                this.exit('Delete product button not found');
            }
            this.evaluate(function () {
                __utils__.log('Delete product button found', 'info');
            });
        });
    });
    casper.thenClick('.delete',function(){
        // Popup handled auto.
        // Check if the created product still here
        this.wait(1000, function(){
            this.test.assertDoesntExist('#product_table tbody tr:first-child td:nth-child(2)','Created product should be deleted');
        });
    });

    casper.run(function() {
        this.test.done();
    });
});