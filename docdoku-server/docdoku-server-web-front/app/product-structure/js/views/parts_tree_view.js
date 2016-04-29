/*global _,define,App*/
define(['backbone', 'models/component_module', 'views/component_views'
], function (Backbone, ComponentModule, ComponentViews) {
	'use strict';

    var PartsTreeView = Backbone.View.extend({
        el: '#product_nav_list',

        events: {
            'change input': 'checkChildrenInputs',
            'change li': 'checkParentsInputs',
            'component:selected a': 'onComponentSelected',
            'click #product_title .product_title': 'onProductTitleClicked',
            'load:root': 'onProductTitleClicked',
            'click .fa-refresh': 'refreshProductView',
            'click .fa-quote-right': 'toggleComment',
            'checkbox:selected': 'onCheckboxSelected'
        },

        checkedPath : [],

        initialize: function () {
            _.bindAll(this);
        },

        uncheckAll: function() {
            this.componentViews.setChecked(false);
            this.checkedPath.length = 0;
            Backbone.Events.trigger('path:selected', this.checkedPath);
        },

        onCheckboxSelected:function(e, checked, model){
            if(checked){
                this.checkedPath.push(model);
            }else{
                this.checkedPath.splice(this.checkedPath.indexOf(model), 1);
            }
            Backbone.Events.trigger('path:selected', this.checkedPath);
        },

        setSelectedComponent: function (component) {
            this.componentSelected = component;
        },

        onProductTitleClicked: function () {
            this.setTitleSelected(true);
            this.setSelectedComponent(this.rootComponent);
            App.appView.onComponentSelected(true);
        },

        setTitleSelected:function(selected){
            if(selected){
                this.$('li.active').removeClass('active');
                this.$('#product_title .product_title').addClass('active');
            }else{
                this.$('#product_title .product_title').removeClass('active');
            }
        },

        refreshProductView: function(){
            this.refreshAll();
        },

        render: function () {
            var self = this;

            this.rootCollection = new ComponentModule.Collection([], { isRoot: true });

            this.smartPath = [];

            this.rootComponent = undefined;

            this.listenTo(this.rootCollection, 'reset', function (collection) {

                self.rootComponent = collection.first();

                if(!self.componentSelected){
                    self.setSelectedComponent(self.rootComponent);
                }

                self.trigger('collection:fetched');

                self.onProductTitleClicked();

            });

            this.componentViews = new ComponentViews.Components({
                collection: this.rootCollection,
                resultPathCollection: this.options.resultPathCollection,
                parentView: this.$el,
                parentChecked: false
            });

            return this;
        },

        getSmartPath: function () {
            return this.smartPath;
        },

        checkChildrenInputs: function (event) {
            var inputs = event.target.parentNode.querySelectorAll('input.load-3D.available');
            for (var i = 0; i < inputs.length; i++) {
                inputs[i].checked = event.target.checked;
                // on retire les fils du smartPath
                this.removeFromSmartPath(inputs[i].id.substring(5));
            }
        },

        // Set smartPaths while checking parents
        checkParentsInputs: function (event) {
            var relativeInput = event.currentTarget.querySelector('input.load-3D');
            relativeInput.checked = event.target.checked;
            var childrenUl = event.currentTarget.querySelector('ul');

            if (childrenUl !== null) {
                // Check children
                var tempArray = [];
                var inputsChecked = 0;

                for (var i = 0; i < childrenUl.childNodes.length; i++) {
                    var li = childrenUl.childNodes[i];
                    if (li.querySelector('input.load-3D') && li.querySelector('input.load-3D').checked) {
                        inputsChecked++;
                        // add children into a temporary array
                        tempArray.push(li.id.substring(5));
                    }
                    // remove children from path
                    this.removeFromSmartPath(li.id.substring(5));
                }
                if (inputsChecked === childrenUl.childNodes.length) {
                    // if all children are checked add the node
                    this.addToSmartPath(relativeInput.parentNode.id.substring(5));
                } else {
                    relativeInput.checked = false;
                    // remove the node and add children checked
                    this.removeFromSmartPath(relativeInput.parentNode.id.substring(5));
                    this.smartPath = this.smartPath.concat(tempArray);
                }
            } else {
                // Check leaves
                if (relativeInput.checked) {
                    // if checked add leaf
                    this.addToSmartPath(relativeInput.parentNode.id.substring(5));
                } else {
                    // if not remove it
                    this.removeFromSmartPath(relativeInput.parentNode.id.substring(5));
                }
            }

            if (relativeInput.parentNode.id === 'path_-1') {
                // Root node : master send the new smartPaths
	            App.collaborativeController.sendSmartPath(this.smartPath);
            }
        },

        addToSmartPath: function (p) {
            this.removeFromSmartPath(p);
            this.smartPath.push(p);
        },
        removeFromSmartPath: function (p) {
            this.smartPath = _.filter(this.smartPath, function (e) {
                return e !== p;
            });
        },

        setSmartPaths: function (arrayPaths) {

	        arrayPaths = (arrayPaths) ? arrayPaths : [];
            var pathsToLoad = _.difference(arrayPaths, this.smartPath);
            var pathsToUnload = _.difference(this.smartPath, arrayPaths);

            // Remove child path of path to load from the pathsToUnload
            pathsToUnload = _.filter(pathsToUnload,function(unloadPath){
                var isChildOfALoadedPath = false;
                _.each(pathsToLoad,function(loadPath){
                    if(loadPath==='null'){
                        isChildOfALoadedPath = true;
                    }else{
                        var isChildOfThis = unloadPath.indexOf(loadPath)===0;
                        isChildOfALoadedPath = isChildOfALoadedPath || isChildOfThis;
                    }
                });
                return ! isChildOfALoadedPath;
            });

            // We have to unload path before load it because some path to unload can be child of path to load
            if (pathsToUnload.length !== 0) {
                App.log('%c Path to unload : \n\t'+pathsToUnload, 'PTV');
                App.instancesManager.unLoadComponentsByPaths(pathsToUnload);
            }
            if (pathsToLoad.length !== 0) {
		        App.log('%c Paths to load : \n\t'+pathsToLoad, 'PTV');
		        App.instancesManager.loadComponentsByPaths(pathsToLoad);
	        }

            this.smartPath = arrayPaths;
            this.setCheckboxes();
        },

        setCheckboxes: function () {
            this.$('li input.load-3D').prop('checked', false);
            _.each(this.smartPath, function (path) {
                this.$('li[id^="path_' + path + '"] input.load-3D').prop('checked', true);
            },this);
        },

        onComponentSelected: function (e, componentModel, li) {
            this.setTitleSelected(false);
            e.stopPropagation();
            this.$('li.active').removeClass('active');
            li.addClass('active');
            this.setSelectedComponent(componentModel);
            App.appView.onComponentSelected();
        },

        refreshAll: function () {
            this.checkedPath = [];
            Backbone.Events.trigger('path:selected', this.checkedPath);
            this.rootCollection.path = App.config.linkType ? null : '-1';
            this.componentViews.fetchAll();
            this.onProductTitleClicked();
            App.instancesManager.clear();
            App.collaborativeController.sendSmartPath(App.partsTreeView.getSmartPath());
            App.collaborativeController.sendConfigSpec(App.config.productConfigSpec);
        },

        toggleComment:function(){
            this.$el.toggleClass('displayComment');
        }

    });

    return PartsTreeView;
});
