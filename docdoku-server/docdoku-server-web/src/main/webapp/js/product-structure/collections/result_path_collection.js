define([
    "models/result_path"
], function (
    ResultPath
    ) {

    var ResultPathCollection = Backbone.Collection.extend({

        model: ResultPath,

        url: function(){
            return "/api/workspaces/" + APP_CONFIG.workspaceId + "/products/" + APP_CONFIG.productId + "/paths?partNumber=" + this.searchString;
        }

    });

    return ResultPathCollection;

});