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

// delete the product
casper.then(function() {
    casper.waitForSelector('#product_table tbody tr:first-child td:first-child input',
        function () {
            this.test.assertEquals(partCreationNumber, this.getHTML('#product_table tbody tr:first-child td:nth-child(3)'), 'Product to delete found');
            this.test.assertExists('#product_table tbody tr:first-child td:first-child input', 'Checkbox found');
            this.click("#product_table tbody tr:first-child td:first-child input");
            this.wait(500, function(){
                this.test.assertExists('.delete-product', 'Delete product button found');
                this.click(".delete-product");
            })
            // Popup handled auto.
        }
    );
});

casper.run(function() {
    this.test.done(5);
});