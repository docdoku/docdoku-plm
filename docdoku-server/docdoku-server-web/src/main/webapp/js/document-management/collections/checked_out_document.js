/*global APP_CONFIG*/
'use strict';
define([
	'models/document'
], function (
	Document
) {
	var TagDocumentList = Backbone.Collection.extend({

		model: Document,

        className:'CheckedOutDocumentList',

        url: function() {
            var baseUrl = '/api/workspaces/' + APP_CONFIG.workspaceId + '/checkedouts';
            return baseUrl + '/'+ APP_CONFIG.login + '/documents';
        },

        comparator: function(document) {
            return document.get('id');
        }

	});

	return TagDocumentList;
});
