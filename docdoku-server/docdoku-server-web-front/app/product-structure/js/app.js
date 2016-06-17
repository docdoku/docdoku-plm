/*global define,App,dat,$*/
define([
    'backbone',
    'mustache',
    'views/search_view',
    'views/parts_tree_view',
    'views/bom_view',
    'views/collaborative_view',
    'views/part_metadata_view',
    'views/part_instance_view',
    'views/export_scene_modal_view',
    'views/control_navigation_view',
    'views/control_modes_view',
    'views/control_transform_view',
    'views/control_markers_view',
    'views/control_layers_view',
    'views/control_options_view',
    'views/control_clipping_view',
    'views/control_explode_view',
    'views/control_measure_view',
    'views/baselines/baseline_select_view',
    'dmu/SceneManager',
    'dmu/collaborativeController',
    'dmu/InstancesManager',
    'text!templates/content.html',
    'common-objects/models/part',
    'views/path_data_modal',
    'views/path_to_path_link_modal',
    'common-objects/views/alert'
], function (Backbone, Mustache, SearchView, PartsTreeView, BomView, CollaborativeView, PartMetadataView, PartInstanceView, ExportSceneModalView, ControlNavigationView, ControlModesView, ControlTransformView, ControlMarkersView, ControlLayersView, ControlOptionsView, ControlClippingView, ControlExplodeView, ControlMeasureView, BaselineSelectView, SceneManager, CollaborativeController, InstancesManager, template, Part, PathDataModalView, PathToPathLinkModalView, AlertView) {

    'use strict';

    var AppView = Backbone.View.extend({
        el: '#content',

        events: {
            'click #scene_view_btn': 'sceneButton',
            'click #bom_view_btn': 'bomButton',
            'click #export_scene_btn': 'exportScene',
            'click #fullscreen_scene_btn': 'fullScreenScene',
            'click #path_data_btn': 'openPathDataModal',
            'click #path_to_path_link_btn' : 'openPathToPathLinkModal'
        },

        inBomMode: false,

        initialize: function () {

        },

        render: function () {

            this.$el.html(Mustache.render(template, {
                productId: App.config.productId,
                contextPath: App.config.contextPath,
                i18n: App.config.i18n
            })).show();

            this.bindDomElements();
            this.menuResizable();

            App.sceneManager = new SceneManager();
            App.instancesManager = new InstancesManager();
            App.collaborativeController = new CollaborativeController();
            App.collaborativeView = new CollaborativeView().render();

            App.controlModesView = new ControlModesView().render();
            App.controlTransformView = new ControlTransformView().render();
            App.partMetadataView = new PartMetadataView({model: new Backbone.Model()});
            App.controlNavigationView = new ControlNavigationView().render();

            App.$ControlsContainer.append(App.collaborativeView.$el);
            App.$ControlsContainer.append(App.controlNavigationView.$el);
            App.$ControlsContainer.append(App.controlModesView.$el);
            App.$ControlsContainer.append(App.controlTransformView.$el);
            App.$ControlsContainer.append(App.partMetadataView.$el);

            App.$ControlsContainer.append(new ControlOptionsView().render().$el);
            App.$ControlsContainer.append(new ControlClippingView().render().$el);
            App.$ControlsContainer.append(new ControlExplodeView().render().$el);
            App.$ControlsContainer.append(new ControlMarkersView().render().$el);
            App.$ControlsContainer.append(new ControlLayersView().render().$el);
            App.$ControlsContainer.append(new ControlMeasureView().render().$el);

            this.pathToPathLinkButton.hide();
            this.pathDataModalButton.hide();

            this.initDebugShortcut();
            try {
                App.sceneManager.init();
                this.bindDatGUIControls();
            } catch (ex) {
                console.error('Got exception in dmu');
                App.log(ex);
                this.onNoWebGLSupport();
            }

            return this;
        },

        initDebugShortcut:function(){
            var k = [191,68,69, 66, 85, 71], // :debug or /debug on macosx
                n = 0;
            $(document).keydown(function (e) {
                if (e.keyCode === k[n++]) {
                    if (n === k.length) {
                        App.setDebug(!App.debug);
                        n = 0;
                        return false;
                    }
                }
                else {
                    n = 0;
                }
            });
        },

        initModules:function(){

            App.searchView = new SearchView().render();
            App.partsTreeView = new PartsTreeView({resultPathCollection: App.searchView.collection}).render();
            App.bomView = new BomView().render();
            App.baselineSelectView = new BaselineSelectView({el: '#config_spec_container'}).render();

            this.bomControls.append(App.bomView.bomHeaderView.$el);

            this.listenEvents();

            App.partsTreeView.once('collection:fetched',function(){
                App.appView.trigger('app:ready');
            });

            return this;

        },

        bindDomElements: function () {
            this.$contentContainer = this.$('#product-content');
            this.$productMenu = this.$('#product-menu');
            this.sceneModeButton = this.$('#scene_view_btn');
            this.bomModeButton = this.$('#bom_view_btn');
            this.exportSceneButton = this.$('#export_scene_btn');
            this.pathDataModalButton = this.$('#path_data_btn');
            this.pathToPathLinkButton = this.$('#path_to_path_link_btn');
            this.bomControls = this.$('.bom-controls');
            this.dmuControls = this.$('.dmu-controls');
            App.$ControlsContainer = this.$('#side_controls_container');
            App.$SceneContainer = this.$('#scene_container');
        },

        menuResizable: function () {
            this.$productMenu.resizable({
                containment: this.$el,
                handles: 'e',
                autoHide: true,
                stop: function (e, ui) {
                    var parent = ui.element.parent();
                    var percent = ui.element.width() / parent.width() * 100;
                    ui.element.css({
                        width: percent + '%',
                        height: '100%'
                    });
                    ui.element.toggleClass('alpha', Math.floor(percent) > 15);
                }
            });
        },

        bomMode: function () {
            this.$contentContainer.attr('class', 'bom-mode');
            this.$productMenu.attr('class', 'bom-mode');
            this.bomModeButton.addClass('active');
            this.sceneModeButton.removeClass('active');
        },

        sceneMode: function () {
            this.$contentContainer.attr('class', 'scene-mode');
            this.$productMenu.attr('class', 'scene-mode');
            this.bomModeButton.removeClass('active');
            this.sceneModeButton.addClass('active');
            App.sceneManager.onContainerShown();
        },

        listenEvents: function () {
            App.baselineSelectView.on('config_spec:changed', this.onConfigSpecChange, this);
            Backbone.Events.on('object:selected', this.onObjectSelected, this);
            Backbone.Events.on('selection:reset', this.onResetSelection, this);
            Backbone.Events.on('part:saved', this.refreshTree, this);
            Backbone.Events.on('path:selected', this.updateDisplayPathToPathLinkButton, this);
            Backbone.Events.on('path-data:clicked', this.onPathDataClicked, this);
            this.listenTo(App.bomView,'checkbox:change',App.partsTreeView.uncheckAll);
            this.listenTo(App.bomView.bomHeaderView,'alert',this.alert);
        },

        alert: function(params) {
            this.$('.notifications').first().append(new AlertView(params).render().$el);
        },

        updateDisplayPathToPathLinkButton: function(pathSelected){

            App.bomView.uncheckAll(pathSelected);
            this.pathSelected = pathSelected;

            if (pathSelected.length === 2) {
                if(pathSelected[0].isSubstituteOf(pathSelected[1]) || pathSelected[1].isSubstituteOf(pathSelected[0])){
                    this.pathToPathLinkButton.hide();
                }else{
                    this.pathToPathLinkButton.show();
                }
            } else {
                this.pathToPathLinkButton.hide();
            }

            if(App.baselineSelectView.isSerialNumberSelected()){
                if (pathSelected.length === 1) {
                    this.pathDataModalButton.show();
                    this.checkedComponent = pathSelected[0];
                } else {
                    this.pathDataModalButton.hide();
                    this.checkedComponent = null;
                }
            } else {
                this.pathDataModalButton.hide();
                this.checkedComponent = null;
            }

        },

        openPathToPathLinkModal:function(){
            var pathToPathLinkModal = new PathToPathLinkModalView({
                pathSelected : this.pathSelected,
                productId : App.config.productId,
                serialNumber : App.baselineSelectView.isSerialNumberSelected() ? App.config.productConfigSpec.substring(3) : null,
                baselineId : App.baselineSelectView.isBaselineSelected() ? App.config.productConfigSpec : null
            }).render();
            window.document.body.appendChild(pathToPathLinkModal.el);
            pathToPathLinkModal.openModal();
        },

        updateBom: function (showRoot) {
            if (showRoot) {
                App.bomView.showRoot(App.partsTreeView.componentSelected);
            } else {
                App.bomView.updateContent(App.partsTreeView.componentSelected);
            }
        },

        sceneButton: function () {
            App.router.navigate(App.config.workspaceId + '/' + App.config.productId + '/config-spec/' + App.config.productConfigSpec + '/scene', {trigger: true});
        },

        bomButton: function () {
            App.router.navigate(App.config.workspaceId + '/' + App.config.productId + '/config-spec/' + App.config.productConfigSpec + '/bom', {trigger: true});
        },

        setSpectatorView: function () {
            this.$('.side_control_group:not(.part_metadata_container)').hide();
        },

        leaveSpectatorView: function () {
            this.$('.side_control_group:not(.part_metadata_container)').show();
        },

        transformControlMode: function () {
            this.$('#view_buttons').find('button').removeClass('active');
        },

        leaveTransformControlMode: function () {
            App.controlTransformView.render();
        },

        updateTreeView: function (arrayPaths) {
            App.partsTreeView.setSmartPaths(arrayPaths);
        },

        onComponentSelected: function (showRoot) {
            this.exportSceneButton.show();

            if (App.partsTreeView.componentSelected) {
                this.updateBom(showRoot);
                this.showPartMetadata();
                App.sceneManager.setPathForIFrame(App.partsTreeView.componentSelected.getPath());
            }
        },

        exportScene: function () {
            // Def url
            var splitUrl = window.location.href.split('/');
            var urlRoot = splitUrl[0] + '//' + splitUrl[2];

            var iframeSrc = urlRoot + '/visualization/#product/' + App.config.workspaceId + '/' + App.config.productId +
                '/' + App.sceneManager.cameraObject.position.x +
                '/' + App.sceneManager.cameraObject.position.y +
                '/' + App.sceneManager.cameraObject.position.z;

            if (App.partsTreeView.componentSelected.getPath()) {
                iframeSrc += '/' + App.partsTreeView.componentSelected.getEncodedPath();
            } else {
                iframeSrc += '/-1';
            }

            iframeSrc += '/' + App.config.productConfigSpec;

            // Open modal
            var esmv = new ExportSceneModalView({iframeSrc: iframeSrc});
            window.document.body.appendChild(esmv.render().el);
            esmv.openModal();
        },

        openPathDataModal:function(){
            var pathDataModal = new PathDataModalView({
                serialNumber: App.config.productConfigSpec.substr(3),
                path : this.checkedComponent.getEncodedPath()
            });
            window.document.body.appendChild(pathDataModal.el);
            pathDataModal.initAndOpenModal();
            this.listenTo(pathDataModal,'path-data:created',this.refreshTree.bind(this));
        },

        onPathDataClicked:function(pathSelected){
            this.checkedComponent = pathSelected;
            this.openPathDataModal();
        },

        fullScreenScene: function () {
            App.sceneManager.requestFullScreen();
        },

        refreshTree: function () {
            App.partsTreeView.refreshAll();
        },

        showPartMetadata: function () {
            App.partMetadataView.setModel(App.partsTreeView.componentSelected).render();
        },

        onNoWebGLSupport: function () {
            this.crashWithMessage(App.config.i18n.NO_WEBGL);
        },

        onConfigSpecChange: function (configSpec) {
            this.setConfigSpec(configSpec);
            if (App.collaborativeController) {
                App.collaborativeController.sendConfigSpec(configSpec);
            }
        },

        setConfigSpec: function (configSpec) {
            App.config.productConfigSpec = configSpec || App.config.productConfigSpec;

            if(App.collaborativeView.roomKey){
                App.router.navigate(App.config.workspaceId + '/' + App.config.productId + '/config-spec/' + App.config.productConfigSpec + '/room/' + App.collaborativeView.roomKey, {trigger: false});
            }else{
                App.router.navigate(App.config.workspaceId + '/' + App.config.productId + '/config-spec/' + App.config.productConfigSpec + '/bom', {trigger: false});
            }

            App.sceneManager.clear();
            App.instancesManager.clear();
            App.partsTreeView.refreshAll();
            this.updateBom();
        },

        onObjectSelected: function (object) {

            var partKey = object.partIterationId.substr(0, object.partIterationId.lastIndexOf('-'));
            var part = new Part({partKey: partKey});

            part.fetch({
                success: function () {
                    // Search the part in the tree
                    App.searchView.trigger('instance:selected', object.path);
                    App.controlNavigationView.setObject(object);
                    App.controlTransformView.setObject(object).render();
                    App.partMetadataView.setModel(part).render();
                }
            });

        },

        onResetSelection: function () {
            App.searchView.trigger('selection:reset');
            App.partMetadataView.reset();
            App.controlNavigationView.reset();
            App.controlTransformView.object = undefined;
            App.controlTransformView.reset();
            this.exportSceneButton.hide();
        },

        bindDatGUIControls: function () {
            // Dat.gui controls
            var gui = new dat.GUI({autoPlace: false});
            var valuesControllers = [];
            this.$el.append(gui.domElement);

            valuesControllers.push(gui.add(App.WorkerManagedValues, 'maxInstances').min(0).max(5000).step(1));
            valuesControllers.push(gui.add(App.WorkerManagedValues, 'maxAngle').min(0).max(Math.PI).step(0.01));
            valuesControllers.push(gui.add(App.WorkerManagedValues, 'maxDist').min(1).max(100000).step(100));
            valuesControllers.push(gui.add(App.WorkerManagedValues, 'minProjectedSize').min(0).max(window.innerHeight).step(1));

            valuesControllers.push(gui.add(App.WorkerManagedValues, 'angleRating').min(0).max(1).step(0.01));
            valuesControllers.push(gui.add(App.WorkerManagedValues, 'distanceRating').min(0).max(1).step(0.01));
            valuesControllers.push(gui.add(App.WorkerManagedValues, 'volRating').min(0).max(1).step(0.01));

            valuesControllers.push(gui.add(App.SceneOptions, 'grid'));
            valuesControllers.push(gui.add(App.SceneOptions, 'rotateSpeed').min(0).max(10).step(0.01));
            valuesControllers.push(gui.add(App.SceneOptions, 'zoomSpeed').min(0).max(10).step(0.01));
            valuesControllers.push(gui.add(App.SceneOptions, 'panSpeed').min(0).max(10).step(0.01));

            var ambientLightColorController = gui.addColor(App.SceneOptions, 'ambientLightColor');
            ambientLightColorController.onChange(App.sceneManager.updateAmbientLight);
            valuesControllers.push(ambientLightColorController);

            var cameraLight1ColorController = gui.addColor(App.SceneOptions, 'cameraLight1Color');
            cameraLight1ColorController.onChange(App.sceneManager.updateCameraLight1);
            valuesControllers.push(cameraLight1ColorController);

            var cameraLight2ColorController = gui.addColor(App.SceneOptions, 'cameraLight2Color');
            cameraLight2ColorController.onChange(App.sceneManager.updateCameraLight2);
            valuesControllers.push(cameraLight2ColorController);

            return this;
        },

        crashWithMessage: function (htmlMessage) {
            App.$SceneContainer.html('<span class="crashMessage">' + htmlMessage + '</span>');
            App.$ControlsContainer.hide();
            this.dmuControls.hide();
        }

    });

    return AppView;
});
