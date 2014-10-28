/*global define*/
define([
    'backbone',
    "models/result_path"
], function (Backbone, ResultPath) {

    var ResultPathCollection = Backbone.Collection.extend({

        model: ResultPath,

        contains: function (partUsageLinkId) {
            return this.some(function (resultPath) {
                return resultPath.contains(partUsageLinkId);
            });
        },

        url: function () {
            return App.config.contextPath + "/api/workspaces/" + App.config.workspaceId + "/products/" + App.config.productId + "/paths?partNumber=" + encodeURIComponent(this.searchString);
        }

    });

    return ResultPathCollection;

});