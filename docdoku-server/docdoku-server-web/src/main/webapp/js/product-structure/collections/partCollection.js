window.PartCollection = Backbone.Collection.extend({

        model: Part,
        url:"/api/workspaces/" + APP_CONFIG.workspaceId + "/products/test?configSpec=latest",
        initialize : function() {
        }
});


