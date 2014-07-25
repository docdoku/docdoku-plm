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

        getDesignItemNumber:function(){
            return this.get("designItemNumber");
        },

        getIndexUrl: function() {
            return "/product-structure/" + APP_CONFIG.workspaceId + "/" + encodeURIComponent(this.getId());
        },

        getFrameUrl: function() {
            return  "/visualization/" + APP_CONFIG.workspaceId + "/" + encodeURIComponent(this.getId())+"?cameraX=0&cameraY=10&cameraZ=1000&pathToLoad=null";
        },

        createBaseline:function(baselineArgs,callbacks){
            $.ajax({
                type: "POST",
                url: this.urlRoot + "/" + this.getId() + "/baselines",
                data: JSON.stringify(baselineArgs),
                contentType: "application/json; charset=utf-8",
                success:callbacks.success,
                error:callbacks.error
            });
        },

        deleteBaselines:function(baselines){
            var that = this ;
            var errors = [];
            _.each(baselines,function(baseline){
                that.deleteBaseline(baseline.getId(),{success:function(){},error:function(){
                    errors.push(baseline);
                }});
            });
            return errors;
        },

        deleteBaseline:function(baselineId,callbacks){
            $.ajax({
                type: "DELETE",
                async:false,
                url: this.urlRoot + "/" + this.getId() + "/baselines/"+baselineId,
                contentType: "application/json; charset=utf-8",
                success:callbacks.success,
                error:callbacks.error
            });
        }

    });

    return ConfigurationItem;

});
