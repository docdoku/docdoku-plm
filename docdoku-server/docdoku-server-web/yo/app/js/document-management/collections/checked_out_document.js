/*global define,App*/
define([
    'backbone',
    'models/document'
], function (Backbone,Document) {
	'use strict';
    var TagDocumentList = Backbone.Collection.extend({

        model: Document,

        className: 'CheckedOutDocumentList',

        url: function () {
            var baseUrl = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/checkedouts';
            return baseUrl + '/' + App.config.login + '/documents';
        },

        comparator: function (document) {
            return document.get('id');
        }

    });

    return TagDocumentList;
});
