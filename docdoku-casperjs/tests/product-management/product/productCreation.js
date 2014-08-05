/*global casper,__utils__,workspaceUrl,productCreationNumber,productCreationName,partCreationNumber*/
'use strict';
casper.test.begin('Create a product is available',3, function(){
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
     * Open the product creation modal
     */
    casper.waitForSelector('.new-product',function openProductCreationModal() {
        exists = this.evaluate(function() {
            return __utils__.exists('.new-product');
        });
        if(!exists){
            this.test.fail('New product button not found');
            this.exit('New product button not found');
        }
        this.evaluate(function(){__utils__.log('New product button found', 'info');});
    });

    /**
     * Test create a product without fill productCreationNumber
     */
    casper.thenClick('.new-product', function createProductWithEmptyProductNumber() {
        this.wait(1000, function(){
            // Search Product Name Input
            exists = this.evaluate(function() {
                return __utils__.exists('#inputDescription');
            });
            if(!exists){
                this.test.fail('Product name input not found');
                this.exit('Product name input not found');
            }
            this.evaluate(function(){__utils__.log('Product name input found', 'info');});
            this.sendKeys('#inputDescription', productCreationName, {reset:true});

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

            // Search Product Creation Submit Button
            exists = this.evaluate(function() {
                return __utils__.exists('#product_creation_modal .btn-primary');
            });
            if(!exists){
                this.test.fail('Product creation submit button not found');
                this.exit('Product creation submit button not found');
            }
            this.evaluate(function(){__utils__.log('Product creation submit button found', 'info');});
        });
    });
    casper.thenClick('#product_creation_modal .btn-primary',function(){
        this.wait(1000, function (){
            this.test.assertExist('#product_creation_modal .btn-primary', 'Should not create product '+productCreationName+" without its productNumber");
        });
    });

    /**
     * Test create a product without fill partCreationNumber
     */
    casper.then(function createProductWithEmptyPartNumber() {
        this.wait(1000, function(){
            // Search Product Number Input
            exists = this.evaluate(function() {
                return __utils__.exists('#inputProductId');
            });
            if(!exists){
                this.test.fail('Product number input not found');
                this.exit('Product number input not found');
            }
            this.evaluate(function(){__utils__.log('Product number input found', 'info');});
            this.sendKeys('#inputProductId', productCreationNumber, {reset:true});

            // Search Product Name Input
            exists = this.evaluate(function() {
                return __utils__.exists('#inputDescription');
            });
            if(!exists){
                this.test.fail('Product name input not found');
                this.exit('Product name input not found');
            }
            this.evaluate(function(){__utils__.log('Product name input found', 'info');});
            this.sendKeys('#inputDescription', productCreationName, {reset:true});

            // Search Part Number Input
            exists = this.evaluate(function() {
                return __utils__.exists('#inputPartNumber');
            });
            if(!exists){
                this.test.fail('Part number input not found');
                this.exit('Part number input not found');
            }
            this.evaluate(function(){__utils__.log('Part number input found', 'info');});
            this.sendKeys('#inputPartNumber', '', {reset:true});

            // Search Product Creation Submit Button
            exists = this.evaluate(function() {
                return __utils__.exists('#product_creation_modal .btn-primary');
            });
            if(!exists){
                this.test.fail('Product creation submit button not found');
                this.exit('Product creation submit button not found');
            }
            this.evaluate(function(){__utils__.log('Product creation submit button found', 'info');});
        });
    });
    casper.thenClick('#product_creation_modal .btn-primary',function(){
        this.wait(1000, function (){
            this.test.assertExist('#product_creation_modal .btn-primary', 'Should not create product '+productCreationName+" without its partNumber");
        });
    });


    /**
     * Test create a product with its productNumber, its productName and its partNumber
     */
    casper.then(function createProductWithFilledField() {
        this.wait(1000, function(){
            // Search Product Number Input
            exists = this.evaluate(function() {
                return __utils__.exists('#inputProductId');
            });
            if(!exists){
                this.test.fail('Product number input not found');
                this.exit('Product number input not found');
            }
            this.evaluate(function(){__utils__.log('Product number input found', 'info');});
            this.sendKeys('#inputProductId', productCreationNumber, {reset:true});

            // Search Product Name Input
            exists = this.evaluate(function() {
                return __utils__.exists('#inputDescription');
            });
            if(!exists){
                this.test.fail('Product name input not found');
                this.exit('Product name input not found');
            }
            this.evaluate(function(){__utils__.log('Product name input found', 'info');});
            this.sendKeys('#inputDescription', productCreationName, {reset:true});

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

            // Search Product Creation Submit Button
            exists = this.evaluate(function() {
                return __utils__.exists('#product_creation_modal .btn-primary');
            });
            if(!exists){
                this.test.fail('Product creation submit button disappear');
                this.exit('Product creation submit button disappear');
            }
            this.evaluate(function(){__utils__.log('Product creation submit button still found', 'info');});
        });
    });
    casper.thenClick('#product_creation_modal .btn-primary', function(){
        this.wait(1000, function (){
            this.test.assertDoesntExist('#product_creation_modal .btn-primary', 'Should create product '+productCreationName);
        });
    });

    casper.run(function(){
        this.test.done();
    });
});