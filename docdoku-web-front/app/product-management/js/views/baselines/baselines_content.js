/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/collections/product_baselines',
    'collections/configuration_items',
    'models/configuration_item',
    'text!templates/baselines/baselines_content.html',
    'views/baselines/baselines_list',
    'text!common-objects/templates/buttons/delete_button.html',
    'text!common-objects/templates/buttons/snap_button.html',
    'text!common-objects/templates/buttons/new_product_instance_button.html',
    'text!common-objects/templates/buttons/udf_button.html',
    'common-objects/views/alert',
    'views/baselines/baseline_creation_view',
    'views/product-instances/product_instances_creation',
    'common-objects/views/udf/user_defined_function'
], function (Backbone, Mustache, ProductBaselines, ConfigurationItemCollection,ConfigurationItem, template, BaselinesListView, deleteButton, snapButton, newProductInstanceButton, udfButton, AlertView, BaselineCreationView, ProductInstanceCreationView, UserDefinedFunctionView) {
	'use strict';

    var BaselinesContentView = Backbone.View.extend({
        partials: {
            deleteButton: deleteButton,
            snapButton:snapButton,
            newProductInstanceButton:newProductInstanceButton,
            udfButton: udfButton
        },

        events: {
            'click button.delete': 'deleteBaseline',
            'click button.new-baseline': 'createBaseline',
            'click button.new-product-instance': 'newProductInstance',
            'click button.udf': 'openUdfView'
        },

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {

            var self = this;

            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}, this.partials));
            this.bindDomElements();
            this.createBaselineButton.show();
            this.newProductInstanceButton.hide();
            new ConfigurationItemCollection().fetch().success(function(collection){
                if(!collection.length){
                    self.$notifications.append(new AlertView({
                        type: 'info',
                        message: App.config.i18n.CREATE_PRODUCT_BEFORE_BASELINE
                    }).render().$el);
                }
            });

            this.$inputProductId.typeahead({
                source: function (query, process) {
                    $.getJSON(App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products', function (data) {
                        var ids = [];
                        _(data).each(function (d) {
                            ids.push(d.id);
                        });
                        process(ids);
                    });
                }
            });

            this.bindEvent();
            this.createBaselineView();
            return this;
        },

        bindDomElements: function () {
            this.$notifications = this.$el.find('.notifications').first();
            this.deleteButton = this.$('.delete');
            this.createBaselineButton = this.$('.new-baseline');
            this.newProductInstanceButton = this.$('.new-product-instance');
            this.$inputProductId = this.$('#inputProductId');
        },

        bindEvent: function(){
            var _this = this;
            this.$inputProductId.change(function () {
                _this.createBaselineView();
            });
            this.delegateEvents();
        },

        createBaseline: function () {
            var baselineCreationView = new BaselineCreationView({collection:this.listView.collection,model:new ConfigurationItem()});
            window.document.body.appendChild(baselineCreationView.render().el);
            baselineCreationView.on('warning', this.onWarning);
            baselineCreationView.openModal();
        },

        newProductInstance: function () {
            var productInstanceCreationView = new ProductInstanceCreationView({
                baseline:this.listView.getSelectedBaseline()
            });
            productInstanceCreationView.on('info',this.onInfo);
            window.document.body.appendChild(productInstanceCreationView.render().el);
            productInstanceCreationView.openModal();
        },

        createBaselineView: function () {
            if (this.listView) {
                this.listView.remove();
                this.changeDeleteButtonDisplay(false);
            }
            if (this.$inputProductId.val()) {
                this.listView = new BaselinesListView({
                    collection: new ProductBaselines({}, {productId: this.$inputProductId.val()})
                }).render();
            } else {
                this.listView = new BaselinesListView({
                    collection: new ProductBaselines()
                }).render();
            }
            this.$el.append(this.listView.el);
            this.listView.on('error', this.onError);
            this.listView.on('warning', this.onWarning);
            this.listView.on('delete-button:display', this.changeDeleteButtonDisplay);
            this.listView.on('new-product-instance-button:display', this.changeNewProductInstanceButtonDisplay);
        },

        deleteBaseline: function () {
            this.listView.deleteSelectedBaselines();
        },

        changeDeleteButtonDisplay: function (state) {
            this.deleteButton.toggle(state);
        },

        changeNewProductInstanceButtonDisplay:function(state){
            this.newProductInstanceButton.toggle(state);
        },

        onError:function(model, error){
            var errorMessage = error ? error.responseText : model;

            this.$notifications.append(new AlertView({
                type: 'error',
                message: errorMessage
            }).render().$el);
        },

        onWarning:function(model, error){
            var errorMessage = error ? error.responseText : model;

            this.$notifications.append(new AlertView({
                type: 'warning',
                message: errorMessage
            }).render().$el);
        },

        onInfo:function(message){
            this.$notifications.append(new AlertView({
                type: 'info',
                message: message
            }).render().$el);
        },

        openUdfView:function(){
            var view = new UserDefinedFunctionView();
            view.render();
            view.setBaselineMode();
            document.body.appendChild(view.el);
            view.openModal();
        }

    });

    return BaselinesContentView;
});
