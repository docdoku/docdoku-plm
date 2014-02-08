casper.thenOpen(authUrl);

casper.then(function openProductManagement() {
    this.test.assertExists('#products_link a', 'Products management link found');
    this.click("#products_link a");
});

casper.waitForSelector('#product-nav',
    function openProductNav() {
        this.test.assertExists('#product-nav div a', 'Product link found');
        this.click("#product-nav div a");
    }
);

casper.waitForSelector('.new-product',
    function openProductCreationModal() {
        this.test.assertExists('.new-product', 'New product button found');
        this.click(".new-product");
    }
);

casper.then(function() {
    this.wait(1000, function(){
            this.test.assertExists('#inputProductId', 'Product number input found');
            this.sendKeys('#inputProductId', productCreationNumber);
            this.test.assertExists('#inputDescription', 'Product name input found');
            this.sendKeys('#inputDescription', productCreationName);
            this.test.assertExists('#inputPartNumber', 'Part number input found');
            this.sendKeys('#inputPartNumber', partCreationNumber);
            this.test.assertExists('#product_creation_modal .btn-primary', 'Submit button found');
            this.capture('productCreation1.png');
            this.click('#product_creation_modal .btn-primary');
            this.wait(1000,function(){
                this.capture('productCreation2.png');
            })
        }
    )
});

casper.run(function() {
    this.test.done(7);
});