define([
    "models/result_path"
], function (
    ResultPath
    ) {

    var ResultPathCollection = Backbone.Collection.extend({

        model: ResultPath,

        /*parse:function(response){
           var self = this;
            _(response).each(function(o){
                self.push(new ResultPath({path:o.path}));
            });
        },*/

        contains: function(partUsageLinkId) {
            return this.some(function(resultPath) {
                return resultPath.contains(partUsageLinkId);
            });
        },

        url: function() {
            return "/api/workspaces/" + APP_CONFIG.workspaceId + "/products/" + APP_CONFIG.productId + "/paths?partNumber=" + encodeURIComponent(this.searchString);
        }

    });

    return ResultPathCollection;

});