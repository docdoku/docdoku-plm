define(["common-objects/models/product_instance"],function(ProductInstance){
    var ProductInstances = Backbone.Collection.extend({
        model: ProductInstance,

        initialize:function(attributes, options){
            if(options) {
                this.productId = options.productId;
            }
        },

        url: function(){
            if(this.productId){
                return "/api/workspaces/" + APP_CONFIG.workspaceId + "/products/" + this.productId + "/product-instances";
            }
            return "/api/workspaces/" + APP_CONFIG.workspaceId + "/products/product-instances";
        }

    });
    return ProductInstances;
});