/*global casper,urls,p2pLinks,$*/

casper.test.begin('Path to path link creation tests suite', 8, function pathToPathLinkCreationTestsSuite() {

    'use strict';

    casper.open('');

    /**
     * Open product structure URL
     * */

    casper.then(function () {
        this.open(urls.productStructure);
    });

    /**
     * Assert the tree is displayed
     */

    casper.then(function waitTree() {
        this.waitForSelector('#product_nav_list_container > .treeview > ul > li', function treeDisplayed() {
            this.test.assert(true, 'Tree is displayed');
        }, function fail() {
            this.capture('screenshot/pathToPathLinkCreation/waitTree-error.png');
            this.test.assert(false, 'Product tree can not be found');
        });
    });

    /**
     * Expand tree
     */
    casper.then(function expandTree() {
        this.waitForSelector('#product_nav_list > ul > li > .hitarea', function expandButtonAvailable() {
            this.test.assert(true, 'Expand button is available');
            this.click('#product_nav_list > ul > li > .hitarea');
        }, function fail() {
            this.capture('screenshot/pathToPathLinkCreation/expandTree-error.png');
            this.test.assert(false, 'Expand button can not be found');
        });
    });

    /**
     * Click on two checkboxes parts
     */
    casper.then(function selectParts() {
        this.waitForSelector('#product_nav_list > ul > li > ul > li > .selectable-part-checkbox', function selectParts() {
            this.click('#product_nav_list > ul > li > ul > li:first-child > .selectable-part-checkbox');

            var isHidden = this.evaluate(function () {
                return $('#path_to_path_link_btn:hidden').length > 0;
            });

            this.test.assert(isHidden, 'Path to path link creation button should not be visible');

            this.click('#product_nav_list > ul > li > ul > li:nth-child(2) > .selectable-part-checkbox');

            this.test.assertElementCount('#product_nav_list > ul > li > ul > li > .selectable-part-checkbox:checked', 2, 'Two checkbox should be selected');

            var isVisible = this.evaluate(function () {
                return $('#path_to_path_link_btn:visible').length > 0;
            });

            this.test.assert(isVisible, 'Path to path link creation button should not be visible');

        }, function fail() {
            this.capture('screenshot/pathToPathLinkCreation/selectParts-error.png');
            this.test.assert(false, 'Select checkboxes can not be found');
        });
    });

    /**
     * Click on typed link creation button
     */
    casper.then(function openCreationModal() {
        this.click('#path_to_path_link_btn');
        this.waitForSelector('.modal.path-to-path-link-modal .btn.add-path-to-path-link-btn', function modalIsDisplayed() {
            this.test.assert(true, 'Path to path link modal is displayed');
            this.click('.modal.path-to-path-link-modal .btn.add-path-to-path-link-btn');
            this.test.assertElementCount('#path-to-path-links > .well', 1, 'One path to path link has been added');
            this.sendKeys('#path-to-path-links > .well:first-child .add-type-input', p2pLinks.type, {reset: true});
            this.click('.modal.path-to-path-link-modal div.modal-footer > button.save-button');
        });
    });

    /**
     * Wait for modal to disappear
     */
    casper.then(function waitForLinkToBeCreated() {
        this.waitWhileSelector('.modal.path-to-path-link-modal', function waitForLinkToBeCreated() {
            this.test.assert(true, 'Typed link modal has disappear');
        });
    });


    casper.run(function allDone() {
        this.test.done();
    });

});
