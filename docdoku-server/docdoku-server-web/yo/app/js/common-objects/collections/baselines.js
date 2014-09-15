/*global define,APP_CONFIG*/
'use strict';
define(['backbone', "common-objects/models/baseline"], function (Backbone, Baseline) {

    var Baselines = Backbone.Collection.extend({

        model: Baseline,

        initialize: function (attributes, options) {
            if (options) {
                this.productId = options.productId;
            }
        },

        url: function () {
            if (this.productId) {
                return APP_CONFIG.contextPath + "/api/workspaces/" + APP_CONFIG.workspaceId + "/products/" + this.productId + "/baselines";
            }
            return APP_CONFIG.contextPath + "/api/workspaces/" + APP_CONFIG.workspaceId + "/products/baselines";
        }

    });

    return Baselines;
});