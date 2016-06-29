/*global casper,urls,workspace*/

casper.test.begin('Document tag list tests suite', 1, function documentTagListTestsSuite() {

    'use strict';

    casper.open('');

    /**
     * Open document management URL
     * */

    casper.then(function () {
        return this.open(urls.documentManagement);
    });

    /**
     * Open tag nav
     */

    casper.then(function waitForTagNavLink() {
        return this.waitForSelector('a[href="#' + workspace + '/tags"]', function () {
            this.click('a[href="#' + workspace + '/tags"]');
        }, function fail() {
            this.capture('screenshot/tagList/waitForFolderNavLink-error.png');
            this.test.assert(false, 'Tag nav link can not be found');
        });
    });

    /**
     * Count tag length
     */

    casper.then(function countTags() {
        return this.waitForSelector('#tag-nav > ul > li.tag', function () {
            this.test.assertElementCount('#tag-nav > ul > li.tag', 2, 'Should have 2 tags created');
        }, function fail() {
            this.capture('screenshot/tagList/countTags-error.png');
            this.test.assert(false, 'Tags links can not be found');
        });
    });


    casper.run(function allDone() {
        return this.test.done();
    });
});
