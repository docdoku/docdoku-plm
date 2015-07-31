/*global define,App*/
define([
    'backbone',
    'common-objects/models/document/document'
], function (Backbone, Document) {
	'use strict';
    var FolderDocumentList = Backbone.Collection.extend({

        model: Document,

        url: function () {
            var baseUrl = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId;
            if (this.parent) {
                return  baseUrl + '/folders' + '/' + this.parent.id + '/documents?configSpec='+App.config.documentConfigSpec;
            } else {
                return  baseUrl + '/folders' + '/' + App.config.workspaceId + '/documents?configSpec='+App.config.documentConfigSpec;
            }
        },

        comparator: function (document) {
            return document.get('id');
        }

    });
    FolderDocumentList.className = 'FolderDocumentList';
    return FolderDocumentList;
});
