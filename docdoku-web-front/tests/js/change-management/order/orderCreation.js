/*global casper,urls,workspace,changeItems*/

casper.test.begin('Change order creation tests suite', 3, function changeOrderCreationTestsSuite() {

    'use strict';

    casper.open('');

    /**
     * Open change management URL
     * */

    casper.then(function () {
        return this.open(urls.changeManagement);
    });

    /**
     * Open change orders nav
     */
    casper.then(function waitForChangeOrdersNavLink() {
        return this.waitForSelector('a[href="#' + workspace + '/orders"]', function clickOnChangeOrderNavLink() {
            this.click('a[href="#' + workspace + '/orders"]');
        }, function fail() {
            this.capture('screenshot/orderCreation/waitForChangeOrdersNavLink-error.png');
            this.test.assert(false, 'Change order nav link can not be found');
        });
    });

    /**
     * Open order creation modal
     */
    casper.then(function openNewChangeOrderModal() {
        return this.waitForSelector('.actions .new-order',
            function clickOnChangeOrderCreationLink() {
                this.click('.actions .new-order');
            }, function fail() {
                this.capture('screenshot/orderCreation/openNewChangeOrderModal-error.png');
                this.test.assert(false, 'New order button can not be found');
            }
        );
    });


    /**
     * Try to create an order without a name
     */
    casper.then(function waitForChangeOrderCreationModal() {
        return this.waitForSelector('#order_creation_modal .modal-footer .btn-primary', function createOrderWithoutName() {
            this.click('#order_creation_modal .modal-footer .btn-primary');
            this.test.assertExists('#order_creation_modal #inputOrderName:invalid', 'Should not create an order without a name');
        }, function fail() {
            this.capture('screenshot/orderCreation/waitForChangeOrderCreationModal-error.png');
            this.test.assert(false, 'Change order modal can not be found');
        });
    });

    /**
     * Fill the form and create the order
     */
    casper.then(function fillAndSubmitChangeOrderCreationModal() {
        return this.waitForSelector('#order_creation_modal input#inputOrderName', function fillForm() {
            this.sendKeys('#order_creation_modal input#inputOrderName', changeItems.changeOrder1.number, {reset: true});
            this.click('#order_creation_modal .modal-footer .btn-primary');
        }, function fail() {
            this.capture('screenshot/orderCreation/fillAndSubmitChangeOrderCreationModal-error.png');
            this.test.assert(false, 'Change order modal can not be found');
        });
    });

    /**
     * Wait for modal to close
     */
    casper.then(function waitForChangeOrderCreationModalToBeClosed() {
        return this.waitWhileSelector('#order_creation_modal', function modalClosed() {
            this.test.assert(true, 'Modal is closed');
        }, function fail() {
            this.capture('screenshot/orderCreation/checkForChangeOrderCreation-error.png');
            this.test.assert(false, 'Change order creation modal not closed');
        });
    });

    /**
     * Verify the order is in the list
     */
    casper.then(function checkOrderIsCreated() {
        return this.waitForSelector('#order_table tr td.reference', function modalClosed() {
            this.test.assertSelectorHasText('#order_table tr td.reference', changeItems.changeOrder1.number);
        }, function fail() {
            this.capture('screenshot/orderCreation/checkForChangeOrderCreation-error.png');
            this.test.assert(false, 'Change order not in the list');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });
});
