define(["models/baseline"],function(Baseline){

    var Baselines = Backbone.Collection.extend({

        model: Baseline,

        url: "/api/workspaces/" + APP_CONFIG.workspaceId + "/products/" + APP_CONFIG.productId + "/baseline"

    });

    return Baselines;
});