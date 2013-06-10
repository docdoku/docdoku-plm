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

var partName;
casper.waitForSelector('#part_table',
    function showPartDetails() {
        partName = this.getHTML('#part_table tbody tr td:nth-child(6)');
        this.test.assertExists('#part_table tbody tr:first-child .part_number', 'First part found');
        this.click("#part_table tbody tr:first-child .part_number");
    }
);

casper.waitForSelector('#part-modal',
    function testPartDetails() {
        this.test.assertEquals(partName, this.getHTML('#form-part div:nth-child(2) div span'), 'Part name valid');
    }
);

casper.then(function closeModal() {
    this.test.assertExists('#cancel-iteration', 'Cancel button found');
    this.click("#cancel-iteration");
});

casper.run(function() {
    this.test.done(5);
});