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
        "views/control_modes_view",
        "views/control_markers_view",
        "views/control_layers_view",
        "views/control_options_view",
        "views/baseline_select_view",
        "SceneManager",
        "text!templates/content.html",
        "i18n!localization/nls/product-structure-strings",
        "models/part"
    ], function (Router, NavBarView, SearchView, PartsTreeView, BomView, PartMetadataView, ExportSceneModalView, ControlModesView, ControlMarkersView, ControlLayersView, ControlOptionsView, BaselineSelectView, SceneManager, template, i18n, Part) {

    var AppView = Backbone.View.extend({

        el: $("#content"),

        events: {
            "click #scene_view_btn": "sceneMode",
            "click #bom_view_btn": "bomMode",
            "click #export_scene_btn": "exportScene",
            "click #fullscreen_scene_btn": "fullScreenScene"
        },

        template:Mustache.compile(template),

        initialize: function() {
            window.config_spec = "latest";
        },

        render:function(){

            this.$el.html(this.template({productId:APP_CONFIG.productId, i18n:i18n}));

            this.inBomMode = false;
            this.partMetadataView = undefined;

            this.bindDomElements();
            this.menuResizable();
            new NavBarView();

            this.searchView = new SearchView().render();

            this.partsTreeView = new PartsTreeView({
                resultPathCollection: this.searchView.collection
            }).render();

            this.bomView = new BomView().render();

            this.baselineSelectView = new BaselineSelectView({el:"#config_spec_container"}).render();

            this.$ControlsContainer.append(new ControlModesView().render().$el);
            this.$ControlsContainer.append(new ControlMarkersView().render().$el);
            this.$ControlsContainer.append(new ControlOptionsView().render().$el);
            this.$ControlsContainer.append(new ControlLayersView().render().$el);

            try{
                sceneManager = new SceneManager();
                sceneManager.init();
            }catch(ex){
                this.onNoWebGLSupport();
            }

            this.listenEvents();

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
                        height: "100%"
                    });
                }
            });
        },

        listenEvents:function(){
            this.partsTreeView.on("component_selected", this.onComponentSelected, this);
            Backbone.Events.on("refresh_tree", this.onRefreshTree, this);
            this.baselineSelectView.on("config_spec:changed",this.onConfigSpecChange,this);
            Backbone.Events.on("instance:selected", this.onInstanceSelected, this);
        },

        bindDomElements:function(){
            this.$productMenu = this.$("#product-menu");
            this.sceneModeButton = this.$("#scene_view_btn");
            this.bomModeButton = this.$("#bom_view_btn");
            this.exportSceneButton = this.$("#export_scene_btn");
            this.fullScreenSceneButton = this.$("#fullscreen_scene_btn");
            this.bomContainer = this.$("#bom_table_container");
            this.centerSceneContainer = this.$("#center_container");
            this.$ControlsContainer = this.$("#side_controls_container");
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
                if(this.partMetadataView == undefined){
                    this.partMetadataView = new PartMetadataView({model:this.partsTreeView.componentSelected}).render();
                    this.$ControlsContainer.append(this.partMetadataView.$el);
                }else{
                    this.partMetadataView.setModel(this.partsTreeView.componentSelected).render();
                }
            }
        },

        onNoWebGLSupport:function(){
            this.centerSceneContainer.html("<span class='alert'>"+i18n.NO_WEBGL+"</span>");
        },

        onConfigSpecChange:function(configSpec){
            window.config_spec = configSpec;
            Backbone.Events.trigger("refresh_tree");
            sceneManager.clear();
        },

        onInstanceSelected:function(instance){
            var partKey = instance.partIterationId.substr(0, instance.partIterationId.lastIndexOf("-"));
            var part = new Part({partKey:partKey});
            var self = this;
            part.fetch({success:function() {
                // Search the part in the tree
                self.searchView.trigger("instance:selected", part.getNumber());

                if(!self.isInBomMode()){
                    if(self.partMetadataView == undefined){
                        self.partMetadataView = new PartMetadataView({model:part}).render();
                        self.$ControlsContainer.append(self.partMetadataView.$el);
                    }else{
                        self.partMetadataView.setModel(part).render();
                    }
                }
            }});
        }

    });

    Router.getInstance();
    Backbone.history.start();

    return AppView;
});