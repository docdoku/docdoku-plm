/*global casper,urls*/

casper.test.begin('Document tag deletion tests suite', 5, function documentTagDeletionTestsSuite() {

    'use strict';

    casper.open('');

    /**
     * Open document management URL
     * */

    casper.then(function () {
        this.open(urls.documentManagement);
    });

    /**
     * Click on the tag button
     * */
    casper.then(function openTagCreationModal() {
        this.waitForSelector('.actions .tags', function buttonDisplayed() {
            this.click('.actions .tags');
        }, function fail() {
            this.capture('screenshot/documentTagDeletion/openTagCreationModal-error.png');
            this.test.assert(false, 'Tag deletion button can not be found');
        });
    });


    /**
     * Wait for the modal to be opened
     * */
    casper.then(function waitTagCreationModal() {
        this.waitForSelector('.modal.tag-management .newTag', function modalOpened() {
            this.test.assert(true, 'Tag deletion modal opened');
        }, function fail() {
            this.capture('screenshot/documentTagDeletion/waitTagCreationModal-error.png');
            this.test.assert(false, 'Tag deletion modal can not be found');
        });
    });

    /**
     * Wait for the modal to be opened
     * */
    casper.then(function waitTagCreationModal() {
        this.waitForSelector('.modal.tag-management ul.existing-tags-list li', function modalOpened() {
            this.test.assert(true, 'Tag modal opened');

            this.click('.modal.tag-management ul.existing-tags-list li a');
            this.test.assertElementCount('.modal.tag-management ul.existing-tags-list li', 1, 'Should have remove the tag from the list');

            this.click('.modal.tag-management ul.existing-tags-list li a');
            this.test.assertElementCount('.modal.tag-management ul.existing-tags-list li', 0, 'Should have remove other the tag from the list');

        }, function fail() {
            this.capture('screenshot/documentTagDeletion/waitTagCreationModal-error.png');
            this.test.assert(false, 'Tag modal can not be found');
        });
    });

    /**
     *
     * Save the tags
     * */
    casper.then(function saveTags() {
        this.wait(100, function () {
            this.click('.modal.tag-management .modal-footer .btn-primary');
            this.waitWhileSelector('.modal.tag-management', function modalClosed() {
                this.test.assert(true, 'Tag modal has been closed');
            }, function fail() {
                this.capture('screenshot/documentTagDeletion/saveTags-error.png');
                this.test.assert(false, 'Tag modal can not be closed');
            });
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});
