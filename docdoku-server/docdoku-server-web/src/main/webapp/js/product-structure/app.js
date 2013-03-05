var sceneManager;

define(
    [
        "router",
        "modules/navbar-module/views/navbar_view",
        "views/search_view",
        "views/parts_tree_view",
        "views/bom_view",
        "views/part_metadata_view",
        "views/export_scene_modal_view",
        "views/shortcuts_view",
        "views/control_choice_view",
        "SceneManager",
        "i18n!localization/nls/product-structure-strings",

    ], function (Router, NavBarView, SearchView, PartsTreeView, BomView, PartMetadataView, ExportSceneModalView, ShortcutsView, ControlChoiceView, SceneManager, i18n) {

    var AppView = Backbone.View.extend({

        el: $("#content"),

        events: {
            "click #scene_view_btn": "sceneMode",
            "click #bom_view_btn": "bomMode",
            "click #export_scene_btn": "exportScene",
            "click #fullscreen_scene_btn": "fullScreenScene"
        },

        initialize: function() {

            this.inBomMode = false;

            this.bindDomElements();
            this.menuResizable();
            new NavBarView();

            this.searchView = new SearchView().render();

            this.partsTreeView = new PartsTreeView({
                resultPathCollection: this.searchView.collection
            }).render();

            this.shortcutsview = new ShortcutsView().render();
            this.controlChoiceView = new ControlChoiceView().render();

            this.bomView = new BomView().render();

            this.sideControlsContainer.prepend(this.controlChoiceView.$el);
            this.sideControlsContainer.prepend(this.shortcutsview.$el);

            try{
                sceneManager = new SceneManager();
                sceneManager.init();
            }catch(ex){
                this.onNoWebGLSupport();
            }

            this.listenEvents();

            this.partMetadataView = null;
        },

        menuResizable:function(){
            this.$productMenu.resizable({
                containment: this.$el,
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

        listenEvents:function(){
            this.partsTreeView.on("component_selected", this.onComponentSelected, this);
            Backbone.Events.on("refresh_tree", this.onRefreshTree, this);
        },

        bindDomElements:function(){
            this.$productMenu = this.$("#product-menu");
            this.sceneModeButton = this.$("#scene_view_btn");
            this.bomModeButton = this.$("#bom_view_btn");
            this.exportSceneButton = this.$("#export_scene_btn");
            this.fullScreenSceneButton = this.$("#fullscreen_scene_btn");
            this.bomContainer = this.$("#bom_table_container");
            this.centerSceneContainer = this.$("#center_container");
            this.sideControlsContainer = this.$("#side_controls_container");
            this.partMetadataContainer = this.$("#part_metadata_container");
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
            this.fullScreenSceneButton.show();
            this.bomView.bomHeaderView.hideCheckGroup();

            if(this.partsTreeView.componentSelected){
                this.exportSceneButton.show();
            }

            if(sceneManager.isLoaded){
                sceneManager.resume();
                sceneManager.showStats();
            }

        },

        bomMode: function() {
            this.inBomMode = true;
            this.sceneModeButton.removeClass("active");
            this.bomModeButton.addClass("active");
            this.partMetadataContainer.removeClass("active");
            this.centerSceneContainer.hide();
            this.exportSceneButton.hide();
            this.fullScreenSceneButton.hide();
            this.bomContainer.show();
            this.updateBom();

            if(sceneManager.isLoaded){
                sceneManager.pause();
                sceneManager.hideStats();
            }
        },

        isInBomMode: function() {
            return this.inBomMode;
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

            var iframeSrc = urlRoot + '/visualization/' + APP_CONFIG.workspaceId + '/' + APP_CONFIG.productId
                + '?cameraX=' + sceneManager.camera.position.x
                + '&cameraY=' + sceneManager.camera.position.y
                + '&cameraZ=' + sceneManager.camera.position.z;

            if(this.partsTreeView.componentSelected.getPath()){
                iframeSrc += '&pathToLoad=' + this.partsTreeView.componentSelected.getPath();
            }else{
                iframeSrc+= "&pathToLoad=null";
            }

            // Open modal
            var esmv = new ExportSceneModalView({iframeSrc:iframeSrc});
            $("body").append(esmv.render().el);
            esmv.openModal();
        },

        fullScreenScene:function(){
            sceneManager.requestFullScreen();
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
        },

        onNoWebGLSupport:function(){
            this.centerSceneContainer.html("<span class='alert'>"+i18n.NO_WEBGL+"</span>");
        }

    });

    Router.getInstance();
    Backbone.history.start();

    return AppView;
});