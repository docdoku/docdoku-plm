/*global define,App*/
define([
    'backbone',
    'models/document'
], function (Backbone,Document) {
	'use strict';
    var CheckedoutDocumentList = Backbone.Collection.extend({

        model: Document,

        className: 'CheckedoutDocumentList',

        url: function () {
            return  App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/documents/checkedout';
        }
        
    });

    return CheckedoutDocumentList;
});
