window.AppView = Backbone.View.extend({

    el: $("#workspace"),

    events: {
        "click #scene_view_btn"   : "showScene",
        "click #metadata_view_btn"   : "showMetadata",
        "click #bom_view_btn"   : "showBom"
    },

    showScene:function(){
        $("#bom_table_container").hide();
        $("#part_metadata_container").hide();
        $("#content").show();
    },

    showMetadata:function(){
        $("#content").hide();
        $("#bom_table_container").hide();
        $("#part_metadata_container").show();
    },

    showBom:function(){
        $("#part_metadata_container").hide();
        $("#content").hide();
        $("#bom_table_container").show();
    },

    initialize: function() {
        var allParts = new PartCollection;
        var partNodeView = new PartNodeView({collection:allParts, parentView: $("#product_nav_list")});
        allParts.fetch();
    }

});

window.App = new AppView;