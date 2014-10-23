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
        'common-objects/views/baselines/baseline_select_view',
        'dmu/SceneManager',
        'dmu/collaborativeController',
        'dmu/InstancesManager',
        'text!templates/content.html',
        'models/part'
], function (Backbone, Mustache, SearchView, PartsTreeView, BomView, CollaborativeView, PartMetadataView, PartInstanceView, ExportSceneModalView, ControlNavigationView, ControlModesView, ControlTransformView, ControlMarkersView, ControlLayersView, ControlOptionsView, ControlClippingView, ControlExplodeView, ControlMeasureView, BaselineSelectView, SceneManager, CollaborativeController, InstancesManager, template, Part) {
	'use strict';
    var AppView = Backbone.View.extend({
        el: '#content',

        events: {
            'click #scene_view_btn': 'sceneMode',
            'click #bom_view_btn': 'bomMode',
            'click #export_scene_btn': 'exportScene',
            'click #fullscreen_scene_btn': 'fullScreenScene'
        },

        inBomMode: false,

        initialize: function () {
            App.config.configSpec = 'latest';
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                productId: App.config.productId,
                contextPath: App.config.contextPath,
                i18n: App.config.i18n})).show();

            this.bindDomElements();
            this.menuResizable();

            try {

                App.instancesManager = new InstancesManager();
                App.sceneManager = new SceneManager();
                App.collaborativeController = new CollaborativeController();

                App.collaborativeView = new CollaborativeView().render();
                App.searchView = new SearchView().render();
                App.partsTreeView = new PartsTreeView({resultPathCollection: App.searchView.collection}).render();
                App.controlNavigationView = new ControlNavigationView().render();
                App.bomView = new BomView().render();
                App.baselineSelectView = new BaselineSelectView({el:'#config_spec_container'}).render();
                App.controlModesView = new ControlModesView().render();
                App.controlTransformView = new ControlTransformView().render();

                App.$ControlsContainer.append(App.collaborativeView.$el);
                App.$ControlsContainer.append(App.controlNavigationView.$el);
                App.$ControlsContainer.append(App.controlModesView.$el);
                App.$ControlsContainer.append(App.controlTransformView.$el);

	            // Todo maybe save controls views
                App.$ControlsContainer.append(new ControlOptionsView().render().$el);
                App.$ControlsContainer.append(new ControlClippingView().render().$el);
                App.$ControlsContainer.append(new ControlExplodeView().render().$el);
                App.$ControlsContainer.append(new ControlMarkersView().render().$el);
                App.$ControlsContainer.append(new ControlLayersView().render().$el);
                App.$ControlsContainer.append(new ControlMeasureView().render().$el);

                App.sceneManager.init();

                this.listenEvents();
                this.bindDatGUIControls();

           } catch (ex) {
                console.error('Got exception in dmu');
                console.error(ex);
                this.onNoWebGLSupport();
            }

            return this;
        },

        requestJoinRoom: function (key) {
            if (!App.mainChannel.isReady()) {
                // Retry to connect every 500ms
                App.log('%c [App] %c Websocket is not openned',true);
                var _this = this;
                setTimeout(function () {
                    _this.requestJoinRoom(key);
                }, 250);
            } else {
                App.collaborativeController.sendJoinRequest(key);
            }
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

        listenEvents: function () {
            App.partsTreeView.on('component_selected', this.onComponentSelected, this);
            Backbone.Events.on('refresh_tree', this.onRefreshTree, this);
            App.baselineSelectView.on('config_spec:changed', this.onConfigSpecChange, this);
            Backbone.Events.on('mesh:selected', this.onMeshSelected, this);
            Backbone.Events.on('selection:reset', this.onResetSelection, this);
        },

        bindDomElements: function () {
            this.$productMenu = this.$('#product-menu');
            this.sceneModeButton = this.$('#scene_view_btn');
            this.bomModeButton = this.$('#bom_view_btn');
            this.exportSceneButton = this.$('#export_scene_btn');
            this.fullScreenSceneButton = this.$('#fullscreen_scene_btn');
            this.bomContainer = this.$('#bom_table_container');
            this.centerSceneContainer = this.$('#center_container');
            this.partMetadataContainer = this.$('#part_metadata_container');
            App.$ControlsContainer = this.$('#side_controls_container');
        },

        updateBom: function (showRoot) {
            if (showRoot) {
                App.bomView.showRoot(App.partsTreeView.componentSelected);
            } else {
                App.bomView.updateContent(App.partsTreeView.componentSelected);
            }
        },

        sceneMode: function () {
            this.inBomMode = false;
            this.bomModeButton.removeClass('active');
            this.sceneModeButton.addClass('active');
            this.bomContainer.hide();
            this.centerSceneContainer.show();
            this.fullScreenSceneButton.show();
            App.bomView.bomHeaderView.hideCheckGroup();

            if (App.partsTreeView.componentSelected) {
                this.exportSceneButton.show();
            }

        },

        bomMode: function () {
            this.inBomMode = true;
            this.sceneModeButton.removeClass('active');
            this.bomModeButton.addClass('active');
            this.partMetadataContainer.removeClass('active');
            this.centerSceneContainer.hide();
            this.exportSceneButton.hide();
            this.fullScreenSceneButton.hide();
            this.bomContainer.show();
            this.updateBom();
        },

        isInBomMode: function () {
            return this.inBomMode;
        },

        setSpectatorView: function () {
            this.$('.side_control_group:not(#part_metadata_container)').hide();
        },

        leaveSpectatorView: function () {
            this.$('.side_control_group:not(#part_metadata_container)').show();
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
            if (this.isInBomMode()) {
                this.updateBom(showRoot);
            } else {
                this.exportSceneButton.show();
            }
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
                iframeSrc += '/' + App.partsTreeView.componentSelected.getPath();
            } else {
                iframeSrc += '/null';
            }

            iframeSrc += '/' + App.config.configSpec;

            // Open modal
            var esmv = new ExportSceneModalView({iframeSrc: iframeSrc});
            window.document.body.appendChild(esmv.render().el);
            esmv.openModal();
        },

        fullScreenScene: function () {
            App.sceneManager.requestFullScreen();
        },

        onRefreshTree: function () {
            if (this.isInBomMode()) {
                this.updateBom();
            }
            App.partsTreeView.refreshAll();
        },

        onRefreshComponent: function (partKey) {
            App.partsTreeView.onRefreshComponent(partKey);
        },

        showPartMetadata: function () {
            if (!this.isInBomMode()) {
                if (this.partMetadataView === undefined) {
                    this.partMetadataView = new PartMetadataView({model: App.partsTreeView.componentSelected}).render();
                    App.$ControlsContainer.append(this.partMetadataView.$el);
                } else {
                    this.partMetadataView.setModel(App.partsTreeView.componentSelected).render();
                }
            }
        },

        onNoWebGLSupport: function () {
	        this.crashWithMessage(App.config.i18n.NO_WEBGL);
        },

        onConfigSpecChange: function (configSpec) {
	        this.setConfigSpec(configSpec);
	        if(App.collaborativeController){
		        App.collaborativeController.sendBaseline(configSpec);
	        }
        },

	    setConfigSpec : function(configSpec){
		    App.config.configSpec = configSpec;
		    App.sceneManager.clear();
		    App.instancesManager.clear();
		    Backbone.Events.trigger('refresh_tree');
	    },

        onMeshSelected: function (mesh) {
            var partKey = mesh.partIterationId.substr(0, mesh.partIterationId.lastIndexOf('-'));
            var part = new Part({partKey: partKey});
            var self = this;
            part.fetch({success: function () {
                // Search the part in the tree
                App.searchView.trigger('instance:selected', part.getNumber());
                if (!self.isInBomMode()) {
                    App.controlNavigationView.setMesh(mesh);
                    App.controlTransformView.setMesh(mesh).render();
                    if (self.partMetadataView === undefined) {
                        self.partMetadataView = new PartMetadataView({model: part}).render();
                        App.$ControlsContainer.append(self.partMetadataView.$el);
                    } else {
                        self.partMetadataView.setModel(part).render();
                    }
                }
            }});
        },

        onResetSelection: function () {
            App.searchView.trigger('selection:reset');
            if (!this.isInBomMode() && this.partMetadataView !== undefined) {
                this.partMetadataView.reset();
                App.controlNavigationView.reset();
                App.controlTransformView.mesh = undefined;
                App.controlTransformView.reset();
            }

        },

        bindDatGUIControls: function () {
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

            valuesControllers.push(gui.add(App.SceneOptions, 'grid'));
            valuesControllers.push(gui.add(App.SceneOptions, 'rotateSpeed').min(0).max(10).step(0.01));
            valuesControllers.push(gui.add(App.SceneOptions, 'zoomSpeed').min(0).max(10).step(0.01));
            valuesControllers.push(gui.add(App.SceneOptions, 'panSpeed').min(0).max(10).step(0.01));

            valuesControllers.push(gui.addColor(App.SceneOptions, 'ambientLightColor'));
            valuesControllers.push(gui.addColor(App.SceneOptions, 'cameraLightColor'));
            return this;
        },

		crashWithMessage: function(htmlMessage){
			this.centerSceneContainer.find('#scene_container').html('<span class="crashMessage">'+htmlMessage+ '</span>');
			this.centerSceneContainer.find('#side_controls_container').empty();
			this.exportSceneButton.remove();
			this.fullScreenSceneButton.remove();
		}
    });

    return AppView;
});
