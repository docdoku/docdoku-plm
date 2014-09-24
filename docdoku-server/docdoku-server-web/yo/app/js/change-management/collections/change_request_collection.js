/*global define,App*/
define([
    'backbone',
    'models/change_request'
], function (Backbone,ChangeRequestModel) {
	'use strict';
    var ChangeRequestListCollection = Backbone.Collection.extend({
        model: ChangeRequestModel,
        url: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/changes/requests';
        }
    });

    return ChangeRequestListCollection;
});