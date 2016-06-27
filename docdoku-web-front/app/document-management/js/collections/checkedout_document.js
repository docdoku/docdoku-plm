/*global define,App*/
define([
    'backbone',
    'common-objects/models/document/document_revision'
], function (Backbone, DocumentRevision) {
	'use strict';
    var CheckedoutDocumentList = Backbone.Collection.extend({

        model: DocumentRevision,

        className: 'CheckedoutDocumentList',

        url: function () {
            return  App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/documents/checkedout';
        }

    });

    return CheckedoutDocumentList;
});
