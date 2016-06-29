/*global casper,urls,products*/

casper.test.begin('Shared part creation tests suite', 7, function sharedPartCreationTestsSuite() {

    'use strict';

    casper.open('');

    /**
     * Open product management URL
     * */

    casper.then(function () {
        return this.open(urls.productManagement);
    });

    /**
     * Go to part nav
     */
    casper.then(function waitForPartNavLink() {
        return this.waitForSelector('#part-nav > .nav-list-entry > a', function clickPartNavLink() {
            this.click('#part-nav > .nav-list-entry > a');
        }, function fail() {
            this.capture('screenshot/sharedPartCreation/waitForPartNavLink-error.png');
            this.test.assert(false, 'Part nav link can not be found');
        });
    });

    /**
     * Find the shared part link in the part list
     */

    casper.then(function waitForPartList() {
        var link = '#part_table tbody tr:first-child td.part-revision-share i';
        return this.waitForSelector(link, function onLinkFound() {
            this.click(link);
        }, function fail() {
            this.capture('screenshot/sharedPartCreation/waitForPartList-error.png');
            this.test.assert(false, 'Shared part modal can not be found');
        });
    });

    /**
     * Wait for modal
     */
    casper.then(function waitForSharedPartCreationModal() {
        return this.waitForSelector('#share-modal', function modalOpened() {
            this.test.assert(true, 'Shared part modal is opened');
        }, function fail() {
            this.capture('screenshot/sharedPartCreation/waitForSharedPartCreationModal-error.png');
            this.test.assert(false, 'Shared part modal can not be found');
        });
    });

    /**
     * Set the part as public
     */
    casper.then(function setPartAsPublicShared() {
        this.click('#share-modal .public-shared-switch .switch-off input');
        return this.waitForSelector('#share-modal .public-shared-switch .switch-on', function publicSharedCreated() {
            this.test.assert(true, 'Part is now public shared');
        }, function fail() {
            this.capture('screenshot/sharedPartCreation/setPartAsPublicShared-error.png');
            this.test.assert(false, 'Shared part cannot be shared as public');
        });

    });


    /**
     * Create a private share, with expire date and password
     */
    casper.then(function createPartPrivateShare() {
        this.sendKeys('#private-share .password', products.part1.sharedPassword, {reset: true});
        this.sendKeys('#private-share .confirm-password', products.part1.sharedPassword, {reset: true});
        this.sendKeys('#private-share .expire-date', products.part1.expireDate, {reset: true});
        this.click('#private-share #generate-private-share');

        return this.waitForSelector('#private-share > div > div > a', function onLinkGenerated() {
            var url = this.fetchText('#private-share > div > div > a');
            urls.privatePartPermalink = url;
            this.test.assert(true, 'Private share created expiring tomorrow : ' + url);

        }, function fail() {
            this.capture('screenshot/sharedPartCreation/createPartPrivateShare-error.png');
            this.test.assert(false, 'Shared part cannot be shared as private');
        });
    });


    /**
     * Close the modal
     */
    casper.then(function closeSharedPartModal() {

        this.click('#share-modal > div.modal-header > button');

        return this.waitWhileSelector('#share-modal', function modalClosed() {
            this.test.assert(true, 'Shared part modal closed');
        }, function fail() {
            this.capture('screenshot/sharedPartCreation/closeSharedPartModal-error.png');
            this.test.assert(false, 'Shared part modal cannot be closed');
        });

    });

    /**
     * Reopen the modal to create a second private share, expired one.
     */
    casper.then(function waitForPartList() {
        var link = '#part_table tbody tr:first-child td.part-revision-share i';
        return this.waitForSelector(link, function onLinkFound() {
            this.click(link);
        }, function fail() {
            this.capture('screenshot/sharedPartCreation/waitForPartList-error.png');
            this.test.assert(false, 'Shared part modal can not be found');
        });
    });

    /**
     * Wait for modal
     */
    casper.then(function waitForSharedPartCreationModal() {
        return  this.waitForSelector('#share-modal', function modalOpened() {

            this.test.assert(true, 'Shared part modal is opened');

            this.sendKeys('#private-share .password', products.part1.sharedPassword, {reset: true});
            this.sendKeys('#private-share .confirm-password', products.part1.sharedPassword, {reset: true});
            this.sendKeys('#private-share .expire-date', products.part1.expireDate2, {reset: true});
            this.click('#private-share #generate-private-share');

        }, function fail() {
            this.capture('screenshot/sharedPartCreation/waitForSharedPartCreationModal-error.png');
            this.test.assert(false, 'Shared part modal can not be found');
        });
    });

    /**
     * Save the generated url for test later
     */
    casper.then(function createPartPrivateShare() {
        return this.waitForSelector('#private-share > div > div > a', function onLinkGenerated() {
            var url = this.fetchText('#private-share > div > div > a');
            urls.privatePartPermalinkExpired = url;
            this.test.assert(true, 'Private share created expiring yesterday : ' + url);
        }, function fail() {
            this.capture('screenshot/sharedPartCreation/createPartPrivateShare-error.png');
            this.test.assert(false, 'Shared part cannot be shared as private');
        });

    });

    /**
     * Close the modal
     */
    casper.then(function closeSharedPartModal() {

        this.click('#share-modal > div.modal-header > button');

        return this.waitWhileSelector('#share-modal', function modalClosed() {
            this.test.assert(true, 'Shared part modal closed');
        }, function fail() {
            this.capture('screenshot/sharedPartCreation/closeSharedPartModal-error.png');
            this.test.assert(false, 'Shared part modal cannot be closed');
        });

    });


    casper.run(function allDone() {
        return this.test.done();
    });

});
