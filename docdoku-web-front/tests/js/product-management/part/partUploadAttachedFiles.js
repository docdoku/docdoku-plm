/*global casper,urls*/

casper.test.begin('Part upload attached file tests suite', 3, function partUploadCadTestsSuite() {
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
            this.capture('screenshot/partUpload/waitForPartNavLink-error.png');
            this.test.assert(false, 'Part nav link can not be found');
        });
    });

    /**
     * Wait the part list
     */
    casper.then(function waitForPartList() {
        var link = '#part_table tbody tr:first-child td.part_number';
        return this.waitForSelector(link, function clickPartNavLink() {
            this.click(link);
        }, function fail() {
            this.capture('screenshot/partUpload/waitForPartList-error.png');
            this.test.assert(false, 'Part list can not be found');
        });
    });

    /**
     * Wait the modal
     */
    casper.then(function waitForPartModal() {
        var modalTab = '#part-modal .tabs li a[href="#tab-part-files"]';
        return this.waitForSelector(modalTab, function modalOpened() {
            this.click(modalTab);
        }, function fail() {
            this.capture('screenshot/partUpload/waitForPartModal-error.png');
            this.test.assert(false, 'Part modal can not be found');
        });
    });

    /**
     * Wait the modal tab
     */
    casper.then(function waitForPartModalTab() {
        return this.waitForSelector('#part-modal .upload-btn', function tabSelected() {
            this.test.assert(true, ' file upload tab opened');
        }, function fail() {
            this.capture('screenshot/partUpload/waitForPartModalTab-error.png');
            this.test.assert(false, 'Part modal tab can not be found');
        });
    });

    /**
     * Chose a file and upload
     */
    casper.then(function setFileAndUpload() {
        this.fill('#iteration-files div:nth-child(2) .upload-form', {
            'upload': 'res/part-upload.obj'
        }, false);

        return this.waitFor(function check() {
            return this.evaluate(function () {
                return document.querySelectorAll('#iteration-files div:nth-child(2) ul.file-list li').length === 1;
            });
        }, function then() {
            this.test.assert(true, 'File has been uploaded to part');
        }, function fail() {
            this.capture('screenshot/partUpload/setFileAndUpload-error.png');
            this.test.assert(false, 'Cannot upload the file');
        });
    });

    /**
     * Check if CAD file icons are well set
     */

    casper.then(function checkCADFileIconsSet() {
        return this.waitForSelector('#part_table .fa.fa-paperclip', function check() {
            this.test.assertElementCount('#part_table .fa.fa-paperclip', 1, 'found 1 part with CAD file');
        });
    });


    casper.run(function allDone() {
        return this.test.done();
    });

});
