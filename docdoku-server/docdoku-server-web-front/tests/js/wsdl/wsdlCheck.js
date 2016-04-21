/*global casper,urls*/

casper.test.begin('WSDL check tests suite', urls.WSDL.length, function wsdlCheckTestsSuite() {

    'use strict';

    casper.open('');

    function testWSDL(wsdlURL){
        casper.thenOpen(wsdlURL,function wsdlOpened(response){
            this.test.assertEqual(response.status,200,'Should have a 200 http code for '+wsdlURL);
        },function wsdlFailedOpened(){
            this.capture('screenshot/wsdl/fail.png');
        });
    }

    urls.WSDL.map(testWSDL);

    casper.run(function allDone() {
        this.test.done();
    });

});
