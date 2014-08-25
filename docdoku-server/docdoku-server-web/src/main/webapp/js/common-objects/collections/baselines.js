/*global APP_CONFIG*/
'use strict';
define(["common-objects/models/baseline"],function(Baseline){

    var Baselines = Backbone.Collection.extend({

        model: Baseline,

        initialize:function(attributes, options){
            if(options){
                this.productId = options.productId;
            }
        },

        url: function(){
            if(this.productId){
                return "/api/workspaces/" + APP_CONFIG.workspaceId + "/products/" + this.productId + "/baselines";
            }
            return "/api/workspaces/" + APP_CONFIG.workspaceId + "/products/baselines";
        }

    });

    return Baselines;
});