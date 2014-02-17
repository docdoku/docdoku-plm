/*global casper*/
casper.test.begin('Show a part details is available',5, function(){
    casper.thenOpen(authUrl);

    casper.then(function openProductManagement() {
        this.test.assertExists('#products_management_link a', 'Products management link found');
        this.click('#products_management_link a');
    });

    casper.then(function () {
        this.waitForSelector('#part-nav',
            function openPartNav() {
                this.test.assertExists('#part-nav div a', 'Parts link found');
                this.click("#part-nav div a");
            }
        );
    });

    var partName;
    casper.then(function () {
        casper.waitForSelector('#part_table tbody tr',
            function showPartDetails() {
                partName = this.getHTML('#part_table tbody tr td:nth-child(6)');
                this.test.assertExists('#part_table tbody tr:first-child .part_number', 'First part found');
                this.click("#part_table tbody tr:first-child .part_number");

            }
        );
    });

    casper.then(function () {
        casper.waitForSelector('#part-modal',
            function testPartDetails() {
                this.test.assertEquals(partName, this.getHTML('#form-part div:nth-child(2) div span'), 'Part name valid');
            }
        );
    });

    casper.then(function () {
        casper.then(function closeModal() {
            this.test.assertExists('#cancel-iteration', 'Cancel button found');
            this.click("#cancel-iteration");
        });
    });

    casper.run(function() {
        this.test.done();
    });
});