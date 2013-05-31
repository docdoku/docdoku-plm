define(["common-objects/models/baseline"],function(Baseline){

    var Baselines = Backbone.Collection.extend({

        model: Baseline,

        initialize:function(attributes, options){
            this.productId = options.productId;
        },

        url: function(){
            return "/api/workspaces/" + APP_CONFIG.workspaceId + "/products/" + this.productId + "/baseline"
        }

    });

    return Baselines;
});