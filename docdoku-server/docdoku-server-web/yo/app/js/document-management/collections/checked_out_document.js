/*global define*/
define([
    'backbone',
    'models/document'
], function (Backbone,Document) {
	'use strict';
    var TagDocumentList = Backbone.Collection.extend({

        model: Document,

        className: 'CheckedOutDocumentList',

        url: function () {
            var baseUrl = APP_CONFIG.contextPath + '/api/workspaces/' + APP_CONFIG.workspaceId + '/checkedouts';
            return baseUrl + '/' + APP_CONFIG.login + '/documents';
        },

        comparator: function (document) {
            return document.get('id');
        }

    });

    return TagDocumentList;
});
