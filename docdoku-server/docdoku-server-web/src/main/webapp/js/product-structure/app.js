var sceneManager;

define(["views/search_view", "views/parts_tree_view"], function (SearchView, PartsTreeView) {

    var AppView = Backbone.View.extend({

        el: $("#workspace"),

        events: {
            "click #scene_view_btn"   : "showScene",
            "click #bom_view_btn"   : "showBom"
        },

        showScene:function(){
            $("#bom_table_container").hide();
            $("#part_metadata_container").hide();
            $("#bottom_controls_container").hide();
            $("#center_container").show();
        },

        showBom:function(){
            $("#part_metadata_container").hide();
            $("#center_container").hide();
            $("#bottom_controls_container").hide();
            $("#bom_table_container").show();
        },

        initialize: function() {
            sceneManager = new SceneManager();

            var searchView = new SearchView();

            var partsTreeView = new PartsTreeView({
                resultPathCollection: searchView.collection
            }).render();

            sceneManager.init();
        }

    });

    return AppView;
});