var sceneManager;
var instancesManager;

// Global Namespace for the application
var App = {
    debug: false,

    setDebug:function(state){
        App.debug = state;
        if(state){
            $("body").addClass("debug");
        }else{
            $("body").removeClass("debug");
        }
    },

    WorkerManagedValues: {
        maxInstances: 500,
        maxAngle: Math.PI / 6,
        maxDist: 100000,
        minProjectedSize: 0.000001,
        distanceRating: 0.7,//0.7,
        angleRating: 0.6,//0.5,
        volRating: 0.7//1.0
    },

    SceneOptions: {
        grid: false,
        skeleton: true,
        zoomSpeed: 1.2,
        rotateSpeed: 1.0,
        panSpeed: 0.3,
        cameraNear: 10,
        cameraFar: 5E5,
        defaultCameraPosition: {x: -1000, y: 800, z: 1100}//{x: -21262.730734573677, y: 13214.484586955678, z: 9104.792300874204}
    }

};

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
        "views/control_explode_view",
        "views/control_measure_view",
        "views/baseline_select_view",
        "dmu/SceneManager",
        "dmu/InstancesManager",
        "text!templates/content.html",
        "i18n!localization/nls/product-structure-strings",
        "models/part"
    ], function (Router,
                 NavBarView,
                 SearchView,
                 PartsTreeView,
                 BomView,
                 PartMetadataView,
                 ExportSceneModalView,
                 ControlModesView,
                 ControlMarkersView,
                 ControlLayersView,
                 ControlOptionsView,
                 ControlExplodeView,
                 ControlMeasureView,
                 BaselineSelectView,
                 SceneManager,
                 InstancesManager,
                 template,
                 i18n,
                 Part) {

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

            try{
                instancesManager = new InstancesManager();
                sceneManager = new SceneManager();
                this.$ControlsContainer.append(new ControlModesView().render().$el);
                this.$ControlsContainer.append(new ControlOptionsView().render().$el);
                this.$ControlsContainer.append(new ControlExplodeView().render().$el);
                this.$ControlsContainer.append(new ControlMarkersView().render().$el);
                this.$ControlsContainer.append(new ControlLayersView().render().$el);
                this.$ControlsContainer.append(new ControlMeasureView().render().$el);
                sceneManager.init();
                instancesManager.start();
            }catch(ex){
                console.log("Got exception in dmu");
                this.onNoWebGLSupport();
            }

            this.listenEvents();
            this.bindDatGUIControls();

        },

        menuResizable:function(){
            this.$productMenu.resizable({
                containment: this.$el,
                handles: 'e',
                autoHide: true,
                stop: function(e, ui) {
                    var parent = ui.element.parent();
                    var percent = ui.element.width()/parent.width()*100;
                    ui.element.css({
                        width: percent+"%",
                        height: "100%"
                    });
                    ui.element.toggleClass("alpha",Math.floor(percent)>15);
                }
            });
        },

        listenEvents:function(){
            this.partsTreeView.on("component_selected", this.onComponentSelected, this);
            Backbone.Events.on("refresh_tree", this.onRefreshTree, this);
            this.baselineSelectView.on("config_spec:changed",this.onConfigSpecChange,this);
            Backbone.Events.on("mesh:selected", this.onMeshSelected, this);
            Backbone.Events.on("selection:reset", this.onResetSelection, this);
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
            sceneManager.setPathForIFrame(this.partsTreeView.componentSelected.getPath());
        },

        exportScene:function(){
            // Def url
            var splitUrl = window.location.href.split("/");
            var urlRoot = splitUrl[0] + "//" + splitUrl[2];

            var iframeSrc = urlRoot + '/visualization/' + APP_CONFIG.workspaceId + '/' + APP_CONFIG.productId
                + '?cameraX=' + sceneManager.cameraObject.position.x
                + '&cameraY=' + sceneManager.cameraObject.position.y
                + '&cameraZ=' + sceneManager.cameraObject.position.z;

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
            this.centerSceneContainer.html("<span class='alert no-webgl'>"+i18n.NO_WEBGL+"</span>");
        },

        onConfigSpecChange:function(configSpec){
            window.config_spec = configSpec;
            Backbone.Events.trigger("refresh_tree");
            sceneManager.clear();
        },

        onMeshSelected:function(mesh){
            var partKey = mesh.partIterationId.substr(0, mesh.partIterationId.lastIndexOf("-"));
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
        },

        onResetSelection:function(){
            this.searchView.trigger("selection:reset");
            if(!this.isInBomMode() && this.partMetadataView != undefined ){
                this.partMetadataView.reset();
            }
        },

        bindDatGUIControls:function(){
            // Dat.gui controls
            var gui = new dat.GUI({ autoPlace: false });
            var valuesControllers = [];
            this.$el.append(gui.domElement);

            valuesControllers.push(gui.add(App.WorkerManagedValues, 'maxInstances').min(0).max(5000).step(1));
            valuesControllers.push(gui.add(App.WorkerManagedValues, 'maxAngle').min(0).max(Math.PI).step(0.01));
            valuesControllers.push(gui.add(App.WorkerManagedValues, 'maxDist').min(1).max(100000).step(100));
            valuesControllers.push(gui.add(App.WorkerManagedValues, 'minProjectedSize').min(0).max(window.innerHeight).step(1));

            valuesControllers.push(gui.add(App.WorkerManagedValues, 'angleRating').min(0).max(1).step(0.01));
            valuesControllers.push(gui.add(App.WorkerManagedValues, 'distanceRating').min(0).max(1).step(0.01));
            valuesControllers.push(gui.add(App.WorkerManagedValues, 'volRating').min(0).max(1).step(0.01));

            valuesControllers.push(gui.add(App.SceneOptions,        'grid'));
            valuesControllers.push(gui.add(App.SceneOptions,        'skeleton'));
            valuesControllers.push(gui.add(App.SceneOptions,        'rotateSpeed').min(0).max(10).step(0.01));
            valuesControllers.push(gui.add(App.SceneOptions,        'zoomSpeed').min(0).max(10).step(0.01));
            valuesControllers.push(gui.add(App.SceneOptions,        'panSpeed').min(0).max(10).step(0.01));

            return this;
        }

    });

    Router.getInstance();
    Backbone.history.start();

    return AppView;
});