define([
    "backbone",
    "models/change_order"
], function (Backbone,ChangeOrderModel) {
    var ChangeOrderListCollection = Backbone.Collection.extend({
        model: ChangeOrderModel,
        url: function () {
            return APP_CONFIG.contextPath + "/api/workspaces/" + APP_CONFIG.workspaceId + "/changes/orders";
        }
    });

    return ChangeOrderListCollection;
});