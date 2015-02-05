/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/collections/baselines',
    'collections/configuration_items',
    'text!templates/baseline/baselines_content.html',
    'views/baseline/baselines_list',
    'views/baseline/baseline_duplicate_view',
    'text!common-objects/templates/buttons/delete_button.html',
    'text!common-objects/templates/buttons/duplicate_button.html',
    'common-objects/views/alert'
], function (Backbone, Mustache, BaselinesCollection, ConfigurationItemCollection, template, BaselinesListView, BaselineDuplicateView, deleteButton, duplicateButton, AlertView) {
	'use strict';

    var BaselinesContentView = Backbone.View.extend({
        partials: {
            deleteButton: deleteButton,
            duplicateButton: duplicateButton
        },

        events: {
            'click button.delete': 'deleteBaseline',
            'click button.duplicate': 'duplicateBaseline'
        },

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}, this.partials));
            this.bindDomElements();

            if(!this.configurationItemCollection){
                this.configurationItemCollection = new ConfigurationItemCollection();
            }

            this.configurationItemCollection.fetch({
                success: this.fillProductList,
                error: this.onError
            });

            this.bindEvent();
            this.createBaselineView();
            return this;
        },

        bindDomElements: function () {
            this.$notifications = this.$el.find('.notifications').first();
            this.deleteButton = this.$('.delete');
            this.duplicateButton = this.$('.duplicate');
            this.$inputProductId = this.$('#inputProductId');
        },

        bindEvent: function(){
            var _this = this;
            this.$inputProductId.change(function () {
                _this.createBaselineView();
            });
            this.delegateEvents();
        },

        fillProductList: function (list) {
            var self = this;
            if (list && list.length) {
                list.each(function (product) {
                    self.$inputProductId.append('<option value="' + product.getId() + ' ">' + product.getId() + '</option>');
                });
                this.$inputProductId.combobox({bsVersion: 2});
            }else{
                this.$notifications.append(new AlertView({
                    type: 'info',
                    message: App.config.i18n.CREATE_PRODUCT_BEFORE_BASELINE
                }).render().$el);
            }
        },

        createBaselineView: function () {
            if (this.listView) {
                this.listView.remove();
                this.changeDeleteButtonDisplay(false);
                this.changeDuplicateButtonDisplay(false);
            }
            if (this.$inputProductId.val()) {
                this.listView = new BaselinesListView({
                    collection: new BaselinesCollection({}, {type:'product',productId: this.$inputProductId.val()})
                }).render();
            } else {
                this.listView = new BaselinesListView({
                    collection: new BaselinesCollection({},{type:'product'})
                }).render();
            }
            this.$el.append(this.listView.el);
            this.listView.on('error', this.onError);
            this.listView.on('warning', this.onWarning);
            this.listView.on('delete-button:display', this.changeDeleteButtonDisplay);
            this.listView.on('duplicate-button:display', this.changeDuplicateButtonDisplay);
        },

        deleteBaseline: function () {
            this.listView.deleteSelectedBaselines();
        },

        duplicateBaseline: function () {
            var baselineDuplicateView = new BaselineDuplicateView({model: this.listView.getSelectedBaseline()});
            window.document.body.appendChild(baselineDuplicateView.render().el);
            baselineDuplicateView.openModal();
        },

        changeDeleteButtonDisplay: function (state) {
            if (state) {
                this.deleteButton.show();
            } else {
                this.deleteButton.hide();
            }
        },

        changeDuplicateButtonDisplay: function (state) {
            if (state) {
                this.duplicateButton.show();
            } else {
                this.duplicateButton.hide();
            }
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
        }
    });

    return BaselinesContentView;
});
