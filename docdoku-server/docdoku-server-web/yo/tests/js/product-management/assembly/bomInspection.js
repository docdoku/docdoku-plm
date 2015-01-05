/*global casper,urls,products,homeUrl,workspace*/

casper.test.begin('Bom inspection tests suite',10, function bomInspectionTestsSuite(){

    'use strict';

    casper.open('');

    /**
     * Open product structure URL
     * */

    casper.then(function(){
        this.open(urls.productStructure);
    });

    /**
     * Assert the tree is collapsed (1 node)
     */

    casper.then(function waitTree(){
        this.waitForSelector('#product_nav_list_container > .treeview > ul > li', function treeDisplayed(){
            this.test.assert(true,'Tree is displayed');
            this.test.assertSelectorHasText('#product_nav_list_container > .treeview > ul > li > a > label',products.part1.number + ' (1)','The first node is correctly named');
        },function fail(){
            this.capture('screenshot/assembly/waitTree-error.png');
            this.test.assert(false,'Product tree can not be found');
        });
    });

    /**
     * Click on the first node
     */

    casper.then(function clickRootNode(){
        this.click('#product_nav_list_container > .treeview > ul > li > a > label');
    });

    /**
     * Enter bom mode
    * */

    casper.then(function openBom(){
        this.waitForSelector('#bom_view_btn', function clickOnBomModeButton(){
            this.click('#bom_view_btn');
            this.test.assert(true,'Bom button found');
        },function fail(){
            this.capture('screenshot/assembly/openBom-error.png');
            this.test.assert(false,'Bom link can not be found');
        });
    });

    /**
     * Wait for bom table
    * */

    casper.then(function waitForBomTable(){
        this.waitForSelector('#bom_table', function bomDisplayed(){
            this.test.assert(true,'Bom list displayed');
        },function fail(){
            this.capture('screenshot/assembly/waitForBomTable-error.png');
            this.test.assert(false,'Bom list can not be found');
        });
    });


    /**
     * Assert rows count is 4
     *
     * */

     casper.then(function countBomTableRows(){
        this.waitForSelector('#bom_table > tbody > tr', function rowsAvailabled(){
            this.test.assertElementCount('#bom_table > tbody > tr',4,'4 entries in the bom list');
        },function fail(){
            this.capture('screenshot/assembly/countBomTableRows-error.png');
            this.test.assert(false,'Bom list may be empty');
        });
    });

    /**
     * Expand the root node
     */
    casper.then(function openStructureInTree(){
        this.click('#product_nav_list_container > .treeview > ul > li > .hitarea');
        this.waitForSelector('#product_nav_list_container > .treeview > ul > li > ul',function childNodesDisplayed(){
            this.test.assert(true,'Child nodes are shown');
        },function fail(){
            this.capture('screenshot/assembly/openStructureInTree-error.png');
            this.test.assert(false,'Child nodes not shown');
        });
    });

    /**
     * Count child nodes
     * */
    casper.then(function countChildNodesInTree(){
        this.test.assertElementCount('#product_nav_list_container > .treeview > ul > li > ul > li ',4,'4 child nodes displayed');
    });

    /**
     * Click on the first child of the root node
     */

    casper.then(function clickRootNode(){
        this.click('#product_nav_list_container > .treeview > ul > li > ul > li:first-child > a > label');
    });

    /**
     * Assert rows count is 1 in the bom
     *
     * */

    casper.then(function countBomTableRows(){
        this.waitForSelector('#bom_table > tbody > tr', function rowsAvailabled(){
            this.test.assertElementCount('#bom_table > tbody > tr',1,'1 entry in the bom list');
        },function fail(){
            this.capture('screenshot/assembly/countBomTableRows-error.png');
            this.test.assert(false,'Bom list may be empty');
        });
    });

    /**
     * Check the root node
     */

    casper.then(function checkRootNode(){
        this.test.assertExists('#product_nav_list_container > .treeview > ul > li > input[type=checkbox]:not(:checked)','Checkbox is unchecked');
        this.click('#product_nav_list_container > .treeview > ul > li > input[type=checkbox]');
    });

    /**
     * Count child nodes checked
     * */
    casper.then(function countChildNodesCheckedInTree(){
        this.test.assertElementCount('#product_nav_list_container > .treeview > ul > li > ul > li  > input[type=checkbox]:checked',4,'4 child nodes checked');
    });

    casper.run(function allDone(){
        this.test.done();
    });

});
