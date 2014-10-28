/*global define*/
define([
    'models/document'
], function (Document) {
	'use strict';
    var TaskDocumentList = Backbone.Collection.extend({

        model: Document,

        className: 'TaskDocumentList',

        setFilterStatus: function (status) {
            this.filterStatus = status;
        },

        url: function () {
            var url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/tasks/' + App.config.login + '/documents/';
            if (this.filterStatus) {
                url += '?filter=' + this.filterStatus;
            }
            return url;
        },

        comparator: function (document) {
            return document.get('id');
        }

    });

    return TaskDocumentList;
});
