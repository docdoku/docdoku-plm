define([
    "common-objects/models/part"
], function (
    Part
    ) {
    var PartList = Backbone.Collection.extend({

        model: Part,

        className:"PartList",

        setQuery:function(query){
            this.query = query;
        },

        initialize:function(){
            this.urlBase = "/api/workspaces/" + APP_CONFIG.workspaceId + "/parts/search/";
        },

        fetchPageCount:function(){
            return false;
        },

        hasSeveralPages:function(){
            return false;
        },

        url:function(){
            return this.urlBase + this.query;
        }

    });

    return PartList;
});
