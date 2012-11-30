define([
    "models/bom_item_model"
], function (
    BomItemModel
    ) {

    var BomItemCollection = Backbone.Collection.extend({

        model: BomItemModel,

        url: function(){
            return "/api/workspaces/" + APP_CONFIG.workspaceId + "/products/" + APP_CONFIG.productId + "?configSpec=latest&partUsageLink="+this.path+"&depth=1"
        }

    });

    return ResultPathCollection;

});
