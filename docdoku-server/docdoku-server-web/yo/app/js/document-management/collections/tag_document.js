/*global define,App*/
define([
    'backbone',
    'models/document'
], function (Backbone,Document) {
	'use strict';
    var TagDocumentList = Backbone.Collection.extend({

        model: Document,

        className: 'TagDocumentList',

        url: function () {
            var tagsUrl = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/tags';
            return tagsUrl + '/' + encodeURIComponent(this.parent.get('label')) + '/documents?configSpec='+App.config.configSpec;
        },

        comparator: function (document) {
            return document.get('id');
        }

    });

    return TagDocumentList;
});
