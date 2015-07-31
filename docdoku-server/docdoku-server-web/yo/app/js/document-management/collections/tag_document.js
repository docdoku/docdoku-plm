/*global define,App*/
define([
    'backbone',
    'common-objects/models/document/document_revision'
], function (Backbone, DocumentRevision) {
	'use strict';
    var TagDocumentList = Backbone.Collection.extend({

        model: DocumentRevision,

        className: 'TagDocumentList',

        url: function () {
            var tagsUrl = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/tags';
            return tagsUrl + '/' + encodeURIComponent(this.parent.get('label')) + '/documents?configSpec='+App.config.documentConfigSpec;
        },

        comparator: function (documentRevision) {
            return documentRevision.get('id');
        }

    });

    return TagDocumentList;
});
