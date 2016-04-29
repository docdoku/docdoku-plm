/*global define,App*/
define([
    'backbone',
    'common-objects/models/document/document_revision'
], function (Backbone, DocumentRevision) {
	'use strict';
    var TagDocumentList = Backbone.Collection.extend({

        model: DocumentRevision,

        className: 'CheckedOutDocumentList',

        url: function () {
            var baseUrl = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/checkedouts';
            return baseUrl + '/' + App.config.login + '/documents';
        },

        comparator: function (documentRevision) {
            return documentRevision.get('id');
        }

    });

    return TagDocumentList;
});
