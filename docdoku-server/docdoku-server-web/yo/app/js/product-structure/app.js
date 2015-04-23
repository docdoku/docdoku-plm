/*global define,App,dat*/
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
    'views/product-instance-data-modal-view'
], function (Backbone, Mustache, SearchView, PartsTreeView, BomView, CollaborativeView, PartMetadataView, PartInstanceView, ExportSceneModalView, ControlNavigationView, ControlModesView, ControlTransformView, ControlMarkersView, ControlLayersView, ControlOptionsView, ControlClippingView, ControlExplodeView, ControlMeasureView, BaselineSelectView, SceneManager, CollaborativeController, InstancesManager, template, Part, ProductInstanceDataModalView) {
    'use strict';
    var AppView = Backbone.View.extend({
        el: '#content',

        events: {
            'click #scene_view_btn': 'sceneButton',
            'click #bom_view_btn': 'bomButton',
            'click #export_scene_btn': 'exportScene',
            'click #fullscreen_scene_btn': 'fullScreenScene',
            'click #product_instance_btn': 'openProductInstanceModal'
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
            this.productInstanceModalButton = this.$('#product_instance_btn');
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
            this.bomModeButton.addClass('active');
            this.sceneModeButton.removeClass('active');
        },

        sceneMode: function () {
            this.$contentContainer.attr('class', 'scene-mode');
            this.bomModeButton.removeClass('active');
            this.sceneModeButton.addClass('active');
            App.sceneManager.onContainerShown();
        },

        listenEvents: function () {
            App.baselineSelectView.on('config_spec:changed', this.onConfigSpecChange, this);
            Backbone.Events.on('mesh:selected', this.onMeshSelected, this);
            Backbone.Events.on('selection:reset', this.onResetSelection, this);
            Backbone.Events.on('part:saved', this.refreshTree, this);
        },

        updateBom: function (showRoot) {
            if (showRoot) {
                App.bomView.showRoot(App.partsTreeView.componentSelected);
            } else {
                App.bomView.updateContent(App.partsTreeView.componentSelected);
            }
        },

        sceneButton: function () {
            App.router.navigate(App.config.workspaceId + '/' + App.config.productId + '/config-spec/' + App.config.configSpec + '/scene', {trigger: true});
        },

        bomButton: function () {
            App.router.navigate(App.config.workspaceId + '/' + App.config.productId + '/config-spec/' + App.config.configSpec + '/bom', {trigger: true});
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

            if(App.baselineSelectView.isSerialNumberSelected()){
                this.productInstanceModalButton.show();
            }else{
                this.productInstanceModalButton.hide();
            }
            this.updateBom(showRoot);
            this.showPartMetadata();
            App.sceneManager.setPathForIFrame(App.partsTreeView.componentSelected.getPath());
        },

        exportScene: function () {
            // Def url
            var splitUrl = window.location.href.split('/');
            var urlRoot = splitUrl[0] + '//' + splitUrl[2];

            var iframeSrc = urlRoot + '/visualization/#' + App.config.workspaceId + '/' + App.config.productId +
                '/' + App.sceneManager.cameraObject.position.x +
                '/' + App.sceneManager.cameraObject.position.y +
                '/' + App.sceneManager.cameraObject.position.z;

            if (App.partsTreeView.componentSelected.getPath()) {
                iframeSrc += '/' + App.partsTreeView.componentSelected.getEncodedPath();
            } else {
                iframeSrc += '/-1';
            }

            iframeSrc += '/' + App.config.configSpec;

            // Open modal
            var esmv = new ExportSceneModalView({iframeSrc: iframeSrc});
            window.document.body.appendChild(esmv.render().el);
            esmv.openModal();
        },

        openProductInstanceModal:function(){
            // Open modal
            var productInstanceModal = new ProductInstanceDataModalView({
                serialNumber: App.config.configSpec.substr(3),
                path : App.partsTreeView.componentSelected.getEncodedPath()
            });
            window.document.body.appendChild(productInstanceModal.render().el);
            productInstanceModal.on('ready',function(){
                productInstanceModal.openModal();

            });
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
            App.config.configSpec = configSpec || App.config.configSpec;

            if(App.collaborativeView.roomKey){
                App.router.navigate(App.config.workspaceId + '/' + App.config.productId + '/config-spec/' + App.config.configSpec + '/room/' + App.collaborativeView.roomKey, {trigger: false});
            }else{
                App.router.navigate(App.config.workspaceId + '/' + App.config.productId + '/config-spec/' + App.config.configSpec + '/bom', {trigger: false});
            }

            App.sceneManager.clear();
            App.instancesManager.clear();
            App.partsTreeView.refreshAll();
            this.updateBom();
        },

        onMeshSelected: function (mesh) {
            var partKey = mesh.partIterationId.substr(0, mesh.partIterationId.lastIndexOf('-'));
            var part = new Part({partKey: partKey});

            part.fetch({
                success: function () {
                    // Search the part in the tree
                    App.searchView.trigger('instance:selected', mesh.path);
                    App.controlNavigationView.setMesh(mesh);
                    App.controlTransformView.setMesh(mesh).render();
                    App.partMetadataView.setModel(part).render();

                }
            });

        },

        onResetSelection: function () {
            App.searchView.trigger('selection:reset');
            App.partMetadataView.reset();
            App.controlNavigationView.reset();
            App.controlTransformView.mesh = undefined;
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

            var cameraLightColorController = gui.addColor(App.SceneOptions, 'cameraLightColor');
            cameraLightColorController.onChange(App.sceneManager.updateCameraLight);
            valuesControllers.push(cameraLightColorController);

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
