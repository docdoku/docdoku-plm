/*global define,App*/
define(['backbone', 'models/component_module', 'views/component_views'
], function (Backbone, ComponentModule, ComponentViews) {
	'use strict';
    var PartsTreeView = Backbone.View.extend({
        el: '#product_nav_list',

        events: {
            'change input': 'checkChildrenInputs',
            'change li': 'checkParentsInputs',
            'component_selected a': 'onComponentSelected',
            'click #product_title': 'onProductRootNode'
        },

        setSelectedComponent: function (component) {
            this.componentSelected = component;
        },

        onProductRootNode: function () {
            this.setSelectedComponent(this.rootComponent);
            this.trigger('component_selected', true);
        },

        render: function () {
            var self = this;
            var rootCollection = new ComponentModule.Collection([], { isRoot: true });

            this.smartPath = [];

            this.rootComponent = undefined;

            this.listenTo(rootCollection, 'reset', function (collection) {
                //the default selected component is the root
                self.rootComponent = collection.first();
                self.setSelectedComponent(self.rootComponent);
            });

            this.componentViews = new ComponentViews.Components({
                collection: rootCollection,
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
            var inputs = event.target.parentNode.querySelectorAll('input.available');
            for (var i = 0; i < inputs.length; i++) {
                inputs[i].checked = event.target.checked;
                // on retire les fils du smartPath
                this.removeFromSmartPath(inputs[i].id.substring(5));
            }
        },

        // Set smartPaths while checking parents
        checkParentsInputs: function (event) {
            var relativeInput = event.currentTarget.querySelector('input');
            relativeInput.checked = event.target.checked;
            var childrenUl = event.currentTarget.querySelector('ul');

            if (childrenUl !== null) {
                // Check children
                var tempArray = [];
                var inputsChecked = 0;

                for (var i = 0; i < childrenUl.childNodes.length; i++) {
                    var li = childrenUl.childNodes[i];
                    if (li.querySelector('input') && li.querySelector('input').checked) {
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

            if (relativeInput.parentNode.id === 'path_null') {
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
	        if (pathsToLoad.length !== 0) {
		        App.log('[PartsTreeView] Paths to load : \n\t'+pathsToLoad);
		        App.instancesManager.loadComponentsByPaths(pathsToLoad);
	        }

	        var pathsToUnload = _.difference(this.smartPath, arrayPaths);
            if (pathsToUnload.length !== 0) {
                App.log('[PartsTreeView] Path to unload : \n\t'+pathsToUnload);
                App.instancesManager.unLoadComponentsByPaths(pathsToUnload);
            }

            this.smartPath = arrayPaths;
            this.setCheckboxes();
        },

        setCheckboxes: function () {
            this.$('li input').prop('checked', false);
            var self = this;
            _.each(this.smartPath, function (path) {
                self.$('li[id^="path_' + path + '"] input').prop('checked', true);
            });
        },

        onComponentSelected: function (e, componentModel, li) {
            e.stopPropagation();
            this.$('li.active').removeClass('active');
            li.addClass('active');
            this.setSelectedComponent(componentModel);
            this.trigger('component_selected');
        },

        refreshAll: function () {
            App.instancesManager.clear();
            this.componentViews.fetchAll();
        },

        onRefreshComponent: function (partKey) {
            this.componentViews.refreshComponent(partKey);
        }

    });

    return PartsTreeView;
});
