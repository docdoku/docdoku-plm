/*global define*/
'use strict';
define([
    'backbone',
    "mustache",
    'text!templates/product-instances/product_instances_creation.html',
    'common-objects/models/product_instance',
    'common-objects/collections/configuration_items',
    'common-objects/collections/baselines'
], function (Backbone, Mustache, template, ProductInstanceModel, ConfigurationItemCollection, BaselinesCollection) {

    var ProductInstanceCreationView = Backbone.View.extend({

        model: new ProductInstanceModel(),

        events: {
            'submit #product_instance_creation_form': 'onSubmitForm',
            'hidden #product_instance_creation_modal': 'onHidden'
        },

        initialize: function () {
            this._subViews = [];
            _.bindAll(this);
            this.$el.on('remove', this.removeSubviews);                                                                  // Remove cascade
        },

        removeSubviews: function () {
            _(this._subViews).invoke('remove');
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.bindDomElements();
            this.$inputSerialNumber.customValidity(App.config.i18n.REQUIRED_FIELD);
            new ConfigurationItemCollection().fetch({success: this.fillConfigurationItemList});
            return this;
        },

        fillConfigurationItemList: function (list) {
            var self = this;
            list.each(function (product) {
                self.$inputConfigurationItem.append('<option value="' + product.getId() + '" >' + product.getId() + '</option>');
            });
            self.fillBaselineList();
            this.$inputConfigurationItem.change(function () {
                self.fillBaselineList();
            });
        },
        fillBaselineList: function () {
            var self = this;
            this.$inputBaseline.empty();
            this.$inputBaseline.attr('disabled', 'disabled');
            new BaselinesCollection({}, {productId: self.$inputConfigurationItem.val()}).fetch({
                success: function (list) {
                    list.each(function (baseline) {
                        self.$inputBaseline.append('<option value="' + baseline.getId() + '" >' + baseline.getName() + '</option>');
                    });
                    self.$inputBaseline.removeAttr('disabled');
                }
            });
        },

        bindDomElements: function () {
            this.$modal = this.$('#product_instance_creation_modal');
            this.$inputSerialNumber = this.$('#inputSerialNumber');
            this.$inputConfigurationItem = this.$('#inputConfigurationItem');
            this.$inputBaseline = this.$('#inputBaseline');
        },

        onSubmitForm: function (e) {
            var data = {
                serialNumber: this.$inputSerialNumber.val(),
                configurationItemId: this.$inputConfigurationItem.val(),
                baselineId: this.$inputBaseline.val()
            };

            if (data.serialNumber && data.configurationItemId && data.baselineId) {
                this.model.unset('serialNumber');
                this.model.save(data, {
                    success: this.onProductInstanceCreated,
                    error: this.onError,
                    wait: true
                });
            }

            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        onProductInstanceCreated: function () {
            this.collection.fetch();
            this.closeModal();
        },

        onError: function (model, error) {
            alert(App.config.i18n.CREATION_ERROR + ' : ' + error.responseText);
        },

        openModal: function () {
            this.$modal.modal('show');
        },

        closeModal: function () {
            this.$modal.modal('hide');
        },

        onHidden: function () {
            this.remove();
        }
    });

    return ProductInstanceCreationView;
});
