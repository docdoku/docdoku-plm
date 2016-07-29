/*global $,define,App*/
define(['common-objects/models/baseline'], function (Baseline) {
    'use strict';
    var DocumentBaseline = Baseline.extend({
        getWorkspaceId: function () {
            return this.get('workspaceId');
        },
        setWorkspaceId: function (workspaceId) {
            this.set('workspaceId', workspaceId);
        },

        getBaselineDocuments: function () {
            return this.get('baselinedDocuments');
        },

        getZipUrl: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/document-baselines/export-files?configSpecType=' + encodeURIComponent(this.getId());
        }
    });

    return DocumentBaseline;
});
