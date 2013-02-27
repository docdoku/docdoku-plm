var sceneManager;

define(["router","views/search_view", "views/parts_tree_view", "views/bom_view", "views/part_metadata_view", "modules/navbar-module/views/navbar_view","SceneManager","views/export_scene_modal_view","i18n!localization/nls/product-structure-strings"], function (Router,SearchView, PartsTreeView, BomView, PartMetadataView, NavBarView, SceneManager, ExportSceneModalView,i18n) {

    var AppView = Backbone.View.extend({

        el: $("#content"),

        events: {
            "click #scene_view_btn": "sceneMode",
            "click #bom_view_btn": "bomMode",
            "click #export_scene_btn": "exportScene"
        },

        updateBom: function(showRoot) {
            if(showRoot){
                this.bomView.showRoot(this.partsTreeView.componentSelected);
            }else{
                this.bomView.updateContent(this.partsTreeView.componentSelected);
            }
        },

        sceneMode: function() {
            this.inBomMode = false;
            this.bomModeButton.removeClass("active");
            this.sceneModeButton.addClass("active");
            this.bomContainer.hide();
            this.centerSceneContainer.show();
            this.bomView.bomHeaderView.hideCheckGroup();
            if(this.partsTreeView.componentSelected){
                this.exportSceneButton.show();
            }
        },

        bomMode: function() {
            this.inBomMode = true;
            this.sceneModeButton.removeClass("active");
            this.bomModeButton.addClass("active");
            this.partMetadataContainer.removeClass("active");
            this.centerSceneContainer.hide();
            this.exportSceneButton.hide();
            this.bomContainer.show();
            this.updateBom();
        },

        isInBomMode: function() {
            return this.inBomMode;
        },

        initialize: function() {

            new NavBarView();

            this.sceneModeButton = this.$("#scene_view_btn");
            this.bomModeButton = this.$("#bom_view_btn");
            this.exportSceneButton = this.$("#export_scene_btn");
            this.bomContainer = this.$("#bom_table_container");
            this.centerSceneContainer = this.$("#center_container");
            this.partMetadataContainer = this.$("#part_metadata_container");
            this.partMetadataView = null;

            this.inBomMode = false;

            this.bomView = new BomView().render();

            sceneManager = new SceneManager();

            var searchView = new SearchView();

            this.partsTreeView = new PartsTreeView({
                resultPathCollection: searchView.collection
            }).render();

            this.partsTreeView.on("component_selected", this.onComponentSelected, this);
            Backbone.Events.on("refresh_tree", this.onRefreshTree, this);

            try{
                sceneManager.init();
            }catch(ex){
                this.$("#center_container").html("<span class='alert'>"+i18n.NO_WEBGL+"</span>");
            }

            $("#product-menu").resizable({
                containment: "#content",
                handles: 'e',
                autoHide: true,
                stop: function(e, ui) {
                    var parent = ui.element.parent();
                    ui.element.css({
                        width: ui.element.width()/parent.width()*100+"%",
                        height: ui.element.height()/parent.height()*100+"%"
                    });
                }
            });



        },

        onComponentSelected: function(showRoot) {
            if (this.isInBomMode()) {
                this.updateBom(showRoot);
            }else{
                this.exportSceneButton.show();
            }
            this.showPartMetadata();
            sceneManager.setPathForIframe(this.partsTreeView.componentSelected.getPath());

        },

        exportScene:function(){

            // Def url
            var splitUrl = window.location.href.split("/");
            var urlRoot = splitUrl[0] + "//" + splitUrl[2];

            var paths = self.rootCollection;

            var iframeSrc = urlRoot + '/visualization/' + APP_CONFIG.workspaceId + '/' + APP_CONFIG.productId
                + '?cameraX=' + sceneManager.camera.position.x
                + '&cameraY=' + sceneManager.camera.position.y
                + '&cameraZ=' + sceneManager.camera.position.z;

            if(this.partsTreeView.componentSelected.getPath()){
                iframeSrc += '&pathToLoad=' + this.partsTreeView.componentSelected.getPath();
            }

            // Open modal
            var esmv = new ExportSceneModalView({iframeSrc:iframeSrc});
            $("body").append(esmv.render().el);
            esmv.openModal();



        },

        onRefreshTree:function(){
            if (this.isInBomMode()) {
                this.updateBom();
            }
            this.partsTreeView.refreshAll();
        },

        onRefreshComponent:function(partKey){
          this.partsTreeView.onRefreshComponent(partKey);
        },

        //TODO better panel for part metadata
        showPartMetadata:function() {
            if(!this.isInBomMode()){
                if( this.partMetadataView == null){
                    this.partMetadataView = new PartMetadataView({model: this.partsTreeView.componentSelected});
                }else{
                    this.partMetadataView.setModel(this.partsTreeView.componentSelected);
                }
                this.partMetadataView.render();
                this.partMetadataContainer.addClass("active");
            }
        }

    });

    Router.getInstance();
    Backbone.history.start();

    return AppView;
});