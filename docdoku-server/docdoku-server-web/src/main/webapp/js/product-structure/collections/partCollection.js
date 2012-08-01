window.PartCollection = Backbone.Collection.extend({

        model: Part,
        url:"/api/workspaces/" + APP_CONFIG.workspaceId + "/products/" + APP_CONFIG.productId + "?configSpec=latest",
        initialize : function() {
        }
});


