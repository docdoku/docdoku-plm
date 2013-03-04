define(function() {

    var ConfigurationItem = Backbone.Model.extend({

        urlRoot: '/api/workspaces/' + APP_CONFIG.workspaceId + '/products',

        idAttribute: "_id",

        parse: function(response) {
            response._id = response.id;
            return response;
        },

        getId:function(){
            return this.get("id");
        },

        getIndexUrl: function() {
            return "/product-structure/" + APP_CONFIG.workspaceId + "/" + encodeURIComponent(this.getId());
        },

        getFrameUrl: function() {
            return  "/visualization/" + APP_CONFIG.workspaceId + "/" + encodeURIComponent(this.getId())+"?cameraX=0&cameraY=10&cameraZ=1000&pathToLoad=null";
        }

    });

    return ConfigurationItem;

});
