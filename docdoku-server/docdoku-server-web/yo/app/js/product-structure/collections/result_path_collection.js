/*global define,App*/
define([
    'backbone',
    'models/result_path'
], function (Backbone, ResultPath) {
    'use strict';
    var ResultPathCollection = Backbone.Collection.extend({

        model: ResultPath,

        contains: function (partUsageLinkId) {
            return this.some(function (resultPath) {
                return resultPath.contains(partUsageLinkId);
            });
        },

        url: function () {
            // TODO : use config spec
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + App.config.productId + '/paths?configSpec='+App.config.configSpec+'&partNumber=' + encodeURIComponent(this.searchString);
        }

    });

    return ResultPathCollection;

});
