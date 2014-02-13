/*global casper*/
casper.test.begin('Delete a part is available',5, function(){
    casper.thenOpen(authUrl);

    casper.then(function openProductManagement() {
        this.test.assertExists('#products_management_link a', 'Products management link found');
        this.click('#products_management_link a');
    });

    casper.waitForSelector('#part-nav',
        function openPartNav() {
            this.test.assertExists('#part-nav div a', 'Parts link found');
            this.click("#part-nav div a");
        }
    );

    // delete the part
    casper.waitForSelector('#part_table', function() {
        this.test.assertEquals(partCreationName, this.getHTML('#part_table tbody tr:first-child td:nth-child(6)'), 'Part to delete found');
        this.test.assertExists('#part_table tbody tr:first-child td:first-child input', 'Checkbox found');
        this.click("#part_table tbody tr:first-child td:first-child input");
        this.wait(500, function(){
            this.test.assertExists('.delete-part', 'Delete part button found');
            this.click(".delete-part");
        })
        // Popup handled auto.
    });

    casper.run(function() {
        this.test.done();
    });
});