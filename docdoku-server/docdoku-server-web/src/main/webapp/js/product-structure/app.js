var sceneManager;

define([
    "collections/part_collection",
    "views/part_node_view"
], function (
    PartCollection,
    PartNodeView
) {

    var AppView = Backbone.View.extend({

        el: $("#workspace"),

        events: {
            "click #scene_view_btn"   : "showScene",
            "click #metadata_view_btn"   : "showMetadata",
            "click #bom_view_btn"   : "showBom"
        },

        showScene:function(){
            $("#bom_table_container").hide();
            $("#part_metadata_container").hide();
            $("#bottom_controls_container").hide();
            $("#center_container").show();
        },

        showMetadata:function(){

        },

        showBom:function(){
            $("#part_metadata_container").hide();
            $("#center_container").hide();
            $("#bottom_controls_container").hide();
            $("#bom_table_container").show();
        },

        initialize: function() {
            sceneManager = new SceneManager();
            var allParts = new PartCollection();
            console.log(allParts)
            var partNodeView = new PartNodeView({collection:allParts, parentView: $("#product_nav_list")});
            allParts.fetch();
            sceneManager.init();
        }

    });

    return AppView;
});