define([
    "models/part"
], function (
    Part
    ) {
    var PartList = Backbone.Collection.extend({

        model: Part,

        className:"PartList",

        initialize:function(){
            this.filterUrl = undefined;
        },

        setFilterUrl :function(url){
            this.filterUrl = url;
        },

        url: function() {
            return this.filterUrl;
        }

    });

    return PartList;
});
