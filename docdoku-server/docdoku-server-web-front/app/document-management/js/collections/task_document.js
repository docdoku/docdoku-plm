/*global define,App*/
define([
    'backbone',
    'common-objects/models/document/document_revision'
], function (Backbone, DocumentRevision) {
	'use strict';
    var TaskDocumentList = Backbone.Collection.extend({

        model: DocumentRevision,

        className: 'TaskDocumentList',

        setFilterStatus: function (status) {
            this.filterStatus = status;
        },

        url: function () {
            var url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/tasks/' + App.config.login + '/documents/';
            if (this.filterStatus) {
                url += '?filter=' + this.filterStatus;
            }
            return url;
        },

        comparator: function (documentRevision) {
            return documentRevision.get('id');
        }

    });

    return TaskDocumentList;
});
