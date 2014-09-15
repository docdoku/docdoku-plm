/*global define*/
define(['backbone', "common-objects/models/product_instance"], function (Backbone, ProductInstance) {
    var ProductInstances = Backbone.Collection.extend({
        model: ProductInstance,

        initialize: function (attributes, options) {
            if (options) {
                this.productId = options.productId;
            }
        },

        url: function () {
            if (this.productId) {
                return APP_CONFIG.contextPath + "/api/workspaces/" + APP_CONFIG.workspaceId + "/products/" + this.productId + "/product-instances";
            }
            return APP_CONFIG.contextPath + "/api/workspaces/" + APP_CONFIG.workspaceId + "/products/product-instances";
        }

    });
    return ProductInstances;
});