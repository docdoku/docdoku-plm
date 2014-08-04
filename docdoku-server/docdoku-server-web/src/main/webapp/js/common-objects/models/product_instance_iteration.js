define([], function () {
    var ProductInstanceIteration = Backbone.Model.extend({
        idAttribute: "iteration",

        initialize: function () {
            this.className = "ProductInstanceIteration";
        },

        initBaselinedParts: function(context,callbacks){
            var that = this;
            $.ajax({
                context: context,
                type: "GET",
                url: this.url() + "/baselined-parts",
                success: function(baselinedParts) {
                    that.setBaselinedParts(baselinedParts);
                    callbacks.success(this);
                }
            });
        },

        urlRoot: function(){
            if(this.getConfigurationItemId){
                return "/api/workspaces/" + APP_CONFIG.workspaceId + "/products/" + this.getConfigurationItemId() +
                       "/product-instances/" + this.getSerialNumber()+"/iterations/";
            }else{
                return this.prototype.urlRoot();
            }
        },
        getSerialNumber:function(){
            return this.get("serialNumber");
        },
        getIteration:function(){
            return this.get("iteration");
        },
        setIteration:function(iteration){
            return this.set("iteration",iteration);
        },
        getIterationNote:function(){
            return this.get("iterationNote");
        },
        setIterationNote:function(iterationNote){
            this.set("iterationNote",iterationNote);
        },
        getConfigurationItemId: function(){
            return this.get("configurationItemId");
        },
        getUpdateAuthor: function(){
            return this.get("updateAuthor");
        },
        getUpdateAuthorName: function(){
            return this.get("updateAuthorName");
        },
        getUpdateDate: function(){
            return this.get("updateDate");
        },
        getBaselinedParts:function(){
            return this.get("baselinedPartsList");
        },
        setBaselinedParts:function(baselinedParts){
            this.set("baselinedPartsList",baselinedParts);
        }
    });

   return ProductInstanceIteration;
});