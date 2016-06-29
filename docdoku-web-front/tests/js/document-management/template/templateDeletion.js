/*global casper,urls*/

casper.test.begin('Document template deletion tests suite', 1, function documentTemplateDeletionTestsSuite() {

    'use strict';

    casper.open('');

    /**
     * Open document management URL
     * */

    casper.then(function () {
        return this.open(urls.documentManagement);
    });

    /**
     * Open template nav
     */

    casper.then(function waitForTemplateNavLink() {
        return this.waitForSelector('#template-nav > .nav-list-entry > a', function clickTemplateNavLink() {
            this.click('#template-nav > .nav-list-entry > a');
        }, function fail() {
            this.capture('screenshot/templateDeletion/waitForTemplateNavLink-error.png');
            this.test.assert(false, 'Template nav link not found');
        });
    });

    /**
     * Wait for template to be displayed in list
     */

    casper.then(function waitForTemplateDisplayed() {
        return this.waitForSelector('#document-management-content table.dataTable tr td.reference', function templateIsDisplayed() {
            this.click('#document-management-content table.dataTable tr td:first-child input[type=checkbox]');
        }, function fail() {
            this.capture('screenshot/templateDeletion/waitForTemplateDisplayed-error.png');
            this.test.assert(false, 'Template not found');
        });
    });

    /**
     * Wait for template suppression button
     */

    casper.then(function waitForDeleteButtonDisplayed() {
        return this.waitForSelector('.actions .delete', function deleteButtonIsDisplayed() {
            this.click('.actions .delete');
        }, function fail() {
            this.capture('screenshot/templateDeletion/waitForDeleteButtonDisplayed-error.png');
            this.test.assert(false, 'Delete template button not found');
        });
    });


    /**
     * Confirm template deletion
     */

    casper.then(function confirmTemplateDeletion() {
        return this.waitForSelector('.bootbox', function confirmBoxAppeared() {
            this.click('.bootbox .modal-footer .btn-primary');
        }, function fail() {
            this.capture('screenshot/templateDeletion/confirmTemplateDeletion-error.png');
            this.test.assert(false, 'Template deletion confirmation modal can not be found');
        });
    });


    /**
     * Wait for template to be removed
     */

    casper.then(function waitForTemplateDeletion() {
        return this.waitWhileSelector('#document-management-content table.dataTable tr td.reference', function templateDeleted() {
            this.test.assert(true, 'Template deleted');
        }, function fail() {
            this.capture('screenshot/templateDeletion/waitForTemplateDeletion-error.png');
            this.test.assert(false, 'Template still there');
        });
    });

    casper.run(function allDone() {
        return this.test.done();
    });
});
