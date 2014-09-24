/*global define,App*/
define([
    'backbone',
    'models/change_order'
], function (Backbone,ChangeOrderModel) {
	'use strict';
    var ChangeOrderListCollection = Backbone.Collection.extend({
        model: ChangeOrderModel,
        url: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/changes/orders';
        }
    });

    return ChangeOrderListCollection;
});