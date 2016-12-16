/*global casper,urls,p2pLinks,$*/

casper.test.begin('Path to path link check tests suite', 26, function pathToPathLinkCheckTestsSuite() {

    'use strict';

    casper.open('');

    /**
     * Open product structure URL
     * */

    casper.then(function () {
        return this.open(urls.productStructure);
    });

    /**
     * Assert the tree is displayed
     */

    casper.then(function waitTree() {
        return this.waitForSelector('#product_nav_list_container > ul > li > .treeview > ul > li', function treeDisplayed() {
            this.test.assert(true, 'Tree is displayed');
        }, function fail() {
            this.capture('screenshot/pathToPathLinkCheck/waitTree-error.png');
            this.test.assert(false, 'Product tree can not be found');
        });
    });

    /**
     * Expand tree
     */
    casper.then(function expandTree() {
        return this.waitForSelector('#product_nav_list > ul > li > .hitarea', function expandButtonAvailable() {
            this.test.assert(true, 'Expand button is available');
            this.click('#product_nav_list > ul > li > .hitarea');
        }, function fail() {
            this.capture('screenshot/pathToPathLinkCheck/expandTree-error.png');
            this.test.assert(false, 'Expand button can not be found');
        });
    });

    /**
     * Click on two checkboxes parts
     */
    casper.then(function selectParts() {
        return this.waitForSelector('#product_nav_list > ul > li > ul > li > .selectable-part-checkbox', function selectParts() {
            this.click('#product_nav_list > ul > li > ul > li:first-child > .selectable-part-checkbox');
            this.click('#product_nav_list > ul > li > ul > li:nth-child(2) > .selectable-part-checkbox');
        }, function fail() {
            this.capture('screenshot/pathToPathLinkCheck/selectParts-error.png');
            this.test.assert(false, 'Select checkboxes can not be found');
        });
    });

    /**
     * Click on typed link creation button and check if created link is present
     */
    casper.then(function openCreationModal() {
        this.click('#path_to_path_link_btn');
        return this.waitForSelector('.modal.path-to-path-link-modal #path-to-path-links > .well', function modalIsDisplayed() {
            this.test.assertElementCount('#path-to-path-links > .well', 1, 'One path to path link should be present');
        }, function fail() {
            this.capture('screenshot/pathToPathLinkCheck/openCreationModal-error.png');
            this.test.assert(false, 'No path to path link found');
        });
    });

    /**
     * Assert that we can add some new links in wip mode, then close modal
     */
    casper.then(function verifyWeCanAddPathToPathLink() {
        return this.waitForSelector('.modal.path-to-path-link-modal .btn.add-path-to-path-link-btn', function verifyWeCanAddPathToPathLink() {
            this.test.assert(true, 'We should be able to add new links');
            this.click('.modal.path-to-path-link-modal .modal-footer button.cancel-button');
        });
    });

    /**
     * Wait for the modal to be closed
     */
    casper.then(function waitModalToBeClosed() {
        return this.waitWhileSelector('.modal.path-to-path-link-modal', function waitModalToBeClosed() {
            this.test.assert(true, 'Modal should be closed');
        });
    });

    function runTreeTests() {

        /**
         * Wait for tree to be ready
         */
        casper.then(function treeReRendered() {
            this.waitForSelector('#product_nav_list > ul > li > a > label', function treeReRendered() {
                this.test.assert(true, 'Tree should be ready to be tested');
            });
        });

        /**
         * Check that we can change structure mode
         */
        casper.then(function checkIfStructureCanBeChangedToType() {
            this.waitForSelector('#path_to_path_link_selector_list', function checkIfStructureCanBeChangedToType() {
                this.test.assert(true, 'Select link type should be present');
                this.test.assertSelectorHasText('#path_to_path_link_selector_list option:nth-child(2)', p2pLinks.type, 'Type selector should have "' + p2pLinks.type + '" present');

                this.evaluate(function () {
                    document.querySelector('#path_to_path_link_selector_list').selectedIndex = 1;
                    $('#path_to_path_link_selector_list').change();
                    return true;
                });

            });
        });

        /**
         * Wait for tree to be re-render, then check first node name
         */
        casper.then(function treeReRendered() {
            this.waitForSelector('#product_nav_list > ul > li > a > label', function treeReRendered() {
                this.test.assertSelectorHasText('#product_nav_list > ul > li > a > label', p2pLinks.type, 'First Node of tree should be named "' + p2pLinks.type + '"');
            }, function fail() {
                this.capture('screenshot/pathToPathLinkCheck/TreeNotRenderedError.png');
                this.test.assert(false, 'Could not load tree');
            });
        });

        /**
         * Expand all nodes, then check for node names
         */
        casper.then(function expandFirstNode() {
            this.click('#product_nav_list > ul > li > .hitarea');
        });

        casper.then(function expandSecondNode() {
            this.waitForSelector('#product_nav_list > ul > li > ul > li > .hitarea', function expandFirstNode() {
                this.click('#product_nav_list > ul > li > ul > li > .hitarea');
            });
        });

        casper.then(function checkExpandedNodes() {
            this.waitForSelector('#product_nav_list > ul > li > ul > li > ul > li > .hitarea', function checkExpandedNodes() {
                this.test.assert(true, 'Nodes should be expanded');
                this.test.assertSelectorHasText('#product_nav_list > ul > li > ul > li > a > label', '  < 100-AAA-CasperJsAssemblyP1-A-2 > (1)  ', 'Second node should be named "  < 100-AAA-CasperJsAssemblyP1-A-2 > (1)  "');
                this.test.assertSelectorHasText('#product_nav_list > ul > li > ul > li > ul > li > a > label', '  < 200-AAA-CasperJsAssemblyP2-A-2 > (1)  ', 'Second node should be named "  < 200-AAA-CasperJsAssemblyP2-A-2 > (1)  "');
            });
        });

    }

    runTreeTests();

    /**
     * Run the same tests for baselines
     */
    casper.then(function switchToBaselineMode() {
        this.evaluate(function () {
            document.querySelector('#config_spec_type_selector_list').selectedIndex = 1;
            $('#config_spec_type_selector_list').change();
            return true;
        });
    });

    runTreeTests();

    /**
     * Run the same tests for productInstances
     */
    casper.then(function switchToProductInstancesMode() {
        this.evaluate(function () {
            document.querySelector('#config_spec_type_selector_list').selectedIndex = 2;
            $('#config_spec_type_selector_list').change();
            return true;
        });
    });

    runTreeTests();

    casper.run(function allDone() {
        this.test.done();
    });

});
