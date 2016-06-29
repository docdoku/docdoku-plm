/*global casper,urls*/

casper.test.begin('Instances tests suite', 1, function instancesCheckTestsSuite() {

    'use strict';

    casper.open('');

    /**
     * Test instances REST resource, make sure provider is working
     */

    casper.thenOpen(urls.productStructureInstances,function onResponse(response){
        this.test.assertEqual(response.status,200,'Should have a 200 http code for '+urls.productStructureInstances);
    }, function fail(){
        this.test.assert(false,'Should have a response for '+urls.productStructureInstances);
    });

    casper.run(function allDone() {
        return this.test.done();
    });

});
