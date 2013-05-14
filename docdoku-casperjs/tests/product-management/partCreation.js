casper.thenOpen(authUrl);

casper.then(function openProductManagement() {
    this.test.assertExists('#products_link a', 'Products management link found');
    this.click("#products_link a");
});

casper.waitForSelector('#part-nav',
    function openPartNav() {
        this.test.assertExists('#part-nav div a', 'Parts link found');
        this.click("#part-nav div a");
    }
);

casper.waitForSelector('.new-part',
    function openPartCreationModal() {
        this.test.assertExists('.new-part', 'New part button found');
        this.click(".new-part");
    }
);

casper.then(function() {
        this.wait(1000, function(){
                this.test.assertExists('#inputPartNumber', 'Part number input found');
                this.sendKeys('#inputPartNumber', partCreationNumber);
                this.test.assertExists('#inputPartName', 'Part name input found');
                this.sendKeys('#inputPartName', partCreationName);
                this.test.assertExists('#part_creation_modal .btn-primary', 'Submit button found');
                this.click('#part_creation_modal .btn-primary');
            }
        )
});

// screenshot
casper.then(function() {
    this.wait(1000, function(){
        this.capture('partCreation.png');
    })
});

casper.run(function() {
    this.test.done(6);
});