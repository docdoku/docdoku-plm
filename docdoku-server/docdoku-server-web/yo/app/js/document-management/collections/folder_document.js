/*global define*/
define([
    'backbone',
    'models/document'
], function (Backbone, Document) {
	'use strict';
    var FolderDocumentList = Backbone.Collection.extend({

        model: Document,

        url: function () {
            var baseUrl = APP_CONFIG.contextPath + '/api/workspaces/' + APP_CONFIG.workspaceId;
            if (this.parent) {
                return  baseUrl + '/folders' + '/' + this.parent.id + '/documents?configSpec='+APP_CONFIG.configSpec;
            } else {
                return  baseUrl + '/folders' + '/' + APP_CONFIG.workspaceId + '/documents?configSpec='+APP_CONFIG.configSpec;
            }
        },

        comparator: function (document) {
            return document.get('id');
        }

    });
    FolderDocumentList.className = 'FolderDocumentList';
    return FolderDocumentList;
});
