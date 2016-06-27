/*global define,App*/
define(['backbone', 'common-objects/models/product_instance'],
function (Backbone, ProductInstance) {
	'use strict';
    var ProductInstances = Backbone.Collection.extend({
        model: ProductInstance,

        initialize: function (attributes, options) {
            if (options) {
                this.productId = options.productId;
            }
        },

        url: function () {
            if (this.productId) {
                return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + this.productId + '/product-instances';
            }
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/product-instances';
        }

    });
    return ProductInstances;
});