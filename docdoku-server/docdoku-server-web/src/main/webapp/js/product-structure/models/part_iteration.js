define([
    "common-objects/utils/date",
    "common-objects/collections/attribute_collection"
], function (
    date,
    AttributeCollection
    ) {

    var PartIteration = Backbone.Model.extend({

        idAttribute: "iteration",

        initialize: function () {

            this.className = "PartIteration";

            var attributes = new AttributeCollection(this.get("instanceAttributes"));

            //'attributes' is a special name for Backbone
            this.set("instanceAttributes", attributes);
        },

        defaults :{
            instanceAttributes : []
        },

        getAttributes : function(){
            return this.get("instanceAttributes");
        },

        getWorkspace : function(){
            return this.get("workspaceId");
        },

        getReference : function(){
            return this.getPartKey() + "-" + this.getIteration();
        },

        getIteration : function(){
            return this.get("iteration");
        },

        getPartKey : function(){
            return  this.get("number")+"-"+this.get("version");
        }

    });

   return PartIteration;

});