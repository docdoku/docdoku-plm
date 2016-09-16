/*global $,define,App*/
define(['common-objects/models/baseline'], function (Baseline) {
    'use strict';
    var DocumentBaseline = Baseline.extend({

        getId: function () {
            return this.get('id');
        },

        getName: function () {
            return this.get('name');
        },

        getDescription: function () {
            return this.get('description');
        },

        getType: function () {
            return this.get('type');
        },

        getWorkspaceId: function () {
            return this.get('workspaceId');
        },
        setWorkspaceId: function (workspaceId) {
            this.set('workspaceId', workspaceId);
        },

        getBaselinedDocuments: function () {
            return this.get('baselinedDocuments');
        },
        setBaselinedDocuments: function (baselinedDocuments) {
            return this.set('baselinedDocuments', baselinedDocuments);
        },
        addBaselinedDocument: function (baselinedDocument) {
            var documents = this.getBaselinedDocuments();
            if(documents.indexOf(baselinedDocument) === -1){
                documents.push(baselinedDocument);
            }
        },

        getZipUrl: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/document-baselines/export-files?configSpecType=' + encodeURIComponent(this.getId());
        },

        url: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/document-baselines/' + this.getId();
        }
    });

    return DocumentBaseline;
});
