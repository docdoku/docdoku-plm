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

        getBaselineDocuments: function (ref, callback) {
            var baselinedDocuments = null;
            $.getJSON(this.url() + '/documents?q=' + ref)
                .success(function (data) {
                    baselinedDocuments = data;
                    if (callback && callback.success) {
                        callback.success(data);
                    }
                })
                .error(function (data) {
                    if (callback && callback.error) {
                        callback.error(data);
                    }
                });
            return baselinedDocuments;
        },

        getBaselineFolders: function (ref, callback) {
            var baselinedFolders = null;
            $.getJSON(this.url() + '/folders?q=' + ref)
                .success(function (data) {
                    baselinedFolders = data;
                    if (callback && callback.success) {
                        callback.success(data);
                    }
                })
                .error(function (data) {
                    if (callback && callback.error) {
                        callback.error(data);
                    }
                });
            return baselinedFolders;
        },

        getZipUrl: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/document-baselines/export-files?configSpecType=' + encodeURIComponent(this.getId());
        }
    });

    return DocumentBaseline;
});
