/*global APP_CONFIG*/
'use strict';
define([
	'models/document'
], function (
	Document
) {
	var CheckedoutDocumentList = Backbone.Collection.extend({

        model: Document,

        className : 'CheckedoutDocumentList',

        url : function(){
            return  '/api/workspaces/' + APP_CONFIG.workspaceId + '/documents/checkedout';
        }

	});

	return CheckedoutDocumentList;
});
