/*global casper,apiUrls,helpers,products,workspace,homeUrl*/
casper.test.begin('Cleaning potential data', 0, function cleanTestsSuite() {
    'use strict';

    casper.open('');

    // Roles
    casper.then(function cleanupRoles() {
        var that = this;
        this.open(apiUrls.getRoles, {method: 'GET'}).then(function (response) {
            if (response.status === 200) {
                var roles = JSON.parse(this.getPageContent());
                roles.forEach(function (role) {
                    that.log('Deleting role ' + role.id, 'info');
                    that.open(apiUrls.getRoles + '/' + role.id, {method: 'DELETE'}).then(function () {
                        that.log('Role ' + role.id + ' deleted', 'info');
                    });
                });
            } else {
                this.log('Cannot delete test roles, reason : ' + helpers.findReasonInResponseHeaders(response.headers), 'warning');
            }
        });
    });

    // Workflows
    casper.then(function cleanupWorkflows() {
        var that = this;
        this.open(apiUrls.getWorkflows, {method: 'GET'}).then(function (response) {
            if (response.status === 200) {
                var workflows = JSON.parse(this.getPageContent());
                workflows.forEach(function (workflow) {
                    that.log('Deleting workflow ' + workflow.id, 'info');
                    that.open(apiUrls.getWorkflows + '/' + workflow.id, {method: 'DELETE'}).then(function () {
                        that.log('Workflow ' + workflow.id + ' deleted', 'info');
                    });
                });
            } else {
                this.log('Cannot delete test workflow, reason : ' + helpers.findReasonInResponseHeaders(response.headers), 'warning');
            }
        });
    });

    // Tags
    casper.then(function cleanupTags() {
        var that = this;
        this.open(apiUrls.getTags, {method: 'GET'}).then(function (response) {
            if (response.status === 200) {
                var tags = JSON.parse(this.getPageContent());
                tags.forEach(function (tag) {
                    that.log('Deleting tag ' + tag.id, 'info');
                    that.open(apiUrls.getTags + '/' + tag.id, {method: 'DELETE'}).then(function () {
                        that.log('Tag ' + tag.id + ' deleted', 'info');
                    });
                });
            } else {
                this.log('Cannot delete test tags, reason : ' + helpers.findReasonInResponseHeaders(response.headers), 'warning');
            }
        });
    });

    // Product instances
    casper.then(function cleanupProductInstances() {
        this.open(apiUrls.deleteProductInstance, {method: 'DELETE'}).then(function (response) {
            if (response.status === 200) {
                this.log('Product instance has been deleted', 'info');
            } else {
                this.log('Cannot delete product instance, status ' + response.status + ',reason : ' + helpers.findReasonInResponseHeaders(response.headers), 'warning');
            }
        });
    });


    // Baselines
    casper.then(function cleanupBaselines() {
        var that = this;
        this.open(apiUrls.getBaselines, {method: 'GET'}).then(function (response) {
            if (response.status === 200) {
                var baselines = JSON.parse(this.getPageContent());
                baselines.forEach(function (baseline) {
                    that.log('Deleting baseline ' + baseline.id, 'info');
                    that.open(apiUrls.getBaselines + '/' + baseline.id, {method: 'DELETE'}).then(function () {
                        that.log('Baseline ' + baseline.id + ' deleted', 'info');
                    });
                });

            } else {
                this.log('Cannot get baselines for product, reason : ' + helpers.findReasonInResponseHeaders(response.headers), 'warning');
            }
        });
    });

    // Products
    casper.then(function cleanupProducts() {
        this.open(apiUrls.deleteProduct, {method: 'DELETE'}).then(function (response) {
            if (response.status === 200) {
                this.log('Test products has been deleted', 'info');
            } else {
                this.log('Cannot delete test products, reason : ' + helpers.findReasonInResponseHeaders(response.headers), 'warning');
            }
        });
    });

    // Parts
    casper.then(function cleanupParts() {
        this.open(apiUrls.deletePart, {method: 'DELETE'}).then(function (response) {
            if (response.status === 200) {
                this.log('Test parts has been deleted', 'info');
            } else {
                this.log('Cannot delete test parts, reason : ' + helpers.findReasonInResponseHeaders(response.headers), 'warning');
            }
        });
    });

    // Assembly parts
    var partNumbers = Object.keys(products.assembly.parts);

    partNumbers.forEach(function (partNumber) {
        casper.then(function cleanupParts() {
            this.open(homeUrl + 'api/workspaces/' + workspace + '/parts/' + partNumber + '-A', {method: 'DELETE'}).then(function (response) {
                if (response.status === 200) {
                    this.log('Part deleted');
                } else {
                    this.log('Cannot delete part, reason : ' + helpers.findReasonInResponseHeaders(response.headers), 'warning');
                }
            });
        });
    });

    // Part templates
    casper.then(function cleanupPartTemplates() {
        this.open(apiUrls.deletePartTemplate, {method: 'DELETE'}).then(function (response) {
            if (response.status === 200) {
                this.log('Test part templates has been deleted', 'info');
            } else {
                this.log('Cannot delete test part templates, reason : ' + helpers.findReasonInResponseHeaders(response.headers), 'warning');
            }
        });
        this.open(apiUrls.deletePartTemplate2, {method: 'DELETE'}).then(function (response) {
            if (response.status === 200) {
                this.log('Test part templates2 has been deleted', 'info');
            } else {
                this.log('Cannot delete test part templates2, reason : ' + helpers.findReasonInResponseHeaders(response.headers), 'warning');
            }
        });
    });

    // Part templates
    casper.then(function cleanupDocumentTemplates() {
        this.open(apiUrls.deleteDocumentTemplate, {method: 'DELETE'}).then(function (response) {
            if (response.status === 200) {
                this.log('Test document template has been deleted', 'info');
            } else {
                this.log('Cannot delete test document template, reason : ' + helpers.findReasonInResponseHeaders(response.headers), 'warning');
            }
        });
    });

    // Documents
    casper.then(function cleanupDocuments() {
        var that = this;
        that.log('Deleting documents', 'info');
        this.open(apiUrls.getDocuments, {method: 'GET'}).then(function (response) {
            that.log(response);
            if (response.status === 200) {
                var documents = JSON.parse(this.getPageContent());
                documents.forEach(function (document) {
                    that.log('Deleting document' + document.id, 'info');
                    that.open(apiUrls.getDocuments + '/' + document.id, {method: 'DELETE'}).then(function () {
                        that.log('Document ' + document.id + ' deleted', 'warning');
                    });
                });
            } else {
                this.log('Cannot delete test documents, reason : ' + helpers.findReasonInResponseHeaders(response.headers), 'warning');
            }
        });
    });

    // Folders
    casper.then(function cleanupFolders() {
        this.open(apiUrls.deleteFolder, {method: 'DELETE'}).then(function (response) {
            if (response.status === 200) {
                this.log('Test folders has been deleted', 'info');
            } else {
                this.log('Cannot delete test folders, reason : ' + helpers.findReasonInResponseHeaders(response.headers), 'warning');
            }
        });
    });

    // LOV
    casper.then(function cleanupLovs() {
        this.open(apiUrls.deleteLov1, {method: 'DELETE'}).then(function (response) {
            if (response.status === 200) {
                this.log('Lov has been deleted', 'info');
            } else {
                this.log('Cannot delete lov, reason : ' + helpers.findReasonInResponseHeaders(response.headers), 'warning');
            }
        });
    });

    casper.then(function cleanupLovs() {
        this.open(apiUrls.deleteLov2, {method: 'DELETE'}).then(function (response) {
            if (response.status === 200) {
                this.log('Lov has been deleted', 'info');
            } else {
                this.log('Cannot delete lov, reason : ' + helpers.findReasonInResponseHeaders(response.headers), 'warning');
            }
        });
    });

    // delete all milestone
    casper.then(function cleanupMilestones() {
        var that = this;
        this.open(apiUrls.milestones, {method: 'GET'}).then(function (response) {
            if (response.status === 200) {
                var milestones = JSON.parse(this.getPageContent());
                milestones.forEach(function (milestone) {
                    that.log('Deleting milestone ' + milestone.id, 'info');
                    that.open(apiUrls.milestones + '/' + milestone.id, {method: 'DELETE'}).then(function () {
                        that.log('milestone ' + milestone.id + ' deleted', 'info');
                    });
                });
            } else {
                this.log('Cannot delete test milestone, reason : ' + helpers.findReasonInResponseHeaders(response.headers), 'warning');
            }
        });
    });

    // Queries
    casper.then(function cleanupQueries() {
        var that = this;
        this.open(apiUrls.queries, {method: 'GET'}).then(function (response) {
            if (response.status === 200) {
                var queries = JSON.parse(this.getPageContent());
                queries.forEach(function (query) {
                    that.log('Deleting query ' + query.id, 'info');
                    that.open(apiUrls.queries + '/' + query.id, {method: 'DELETE'}).then(function () {
                        that.log('Query ' + query.id + ' deleted', 'info');
                    });
                });
            } else {
                this.log('Cannot delete test queries, reason : ' + helpers.findReasonInResponseHeaders(response.headers), 'warning');
            }
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});
