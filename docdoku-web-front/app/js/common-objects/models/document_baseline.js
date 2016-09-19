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
            if(!(baselinedDocument.isReleased() || baselinedDocument.isObsolete()) && this.getType() === 'RELEASED'){
                return {error : App.config.i18n.DOCUMENT_NOT_RELEASED};
            }

            var documents = this.getBaselinedDocuments();

            // todo use custom loop to detect number/version
            if(documents.indexOf(baselinedDocument) === -1){
                documents.push(baselinedDocument);
            } else {
                return {info : App.config.i18n.DOCUMENT_ALREADY_IN_LIST};
            }
        },

        getZipUrl: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/document-baselines/export-files?configSpecType=' + encodeURIComponent(this.getId());
        },

        url: function () {
            if (this.getId()) {
                return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/document-baselines/' + this.getId();
            }
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/document-baselines';
        }
    });

    return DocumentBaseline;
});
