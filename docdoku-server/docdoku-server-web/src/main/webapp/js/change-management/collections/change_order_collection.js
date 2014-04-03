define([
    "models/change_order"
], function (
    ChangeOrderModel
    ) {
    var ChangeOrderListCollection = Backbone.Collection.extend({
        model: ChangeOrderModel,
        url: "/api/workspaces/" + APP_CONFIG.workspaceId + "/changes/orders"
    });

    return ChangeOrderListCollection;
});