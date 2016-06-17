/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/product/product_creation_view.html',
    'models/configuration_item',
    'common-objects/views/alert'
], function (Backbone, Mustache, template, ConfigurationItem, AlertView) {
    'use strict';
    var ProductCreationView = Backbone.View.extend({

        events: {
            'click .modal-footer .btn-primary': 'interceptSubmit',
            'submit #product_creation_form': 'onSubmitForm',
            'hidden #product_creation_modal': 'onHidden'
        },

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.bindDomElements();
            this.bindTypeahead();
            this.$('input[required]').customValidity(App.config.i18n.REQUIRED_FIELD);
            return this;
        },

        setRootPart:function(part){
            this.$inputPartNumber.val(part.getNumber());
            this.$inputPartName.val(part.getName());
            this.$inputPart.val(part.getName() + ' < ' + part.getNumber() + ' >');
            return this;
        },

        bindDomElements: function () {
            this.$notifications = this.$el.find('.notifications').first();
            this.$modal = this.$('#product_creation_modal');
            this.$inputPartNumber = this.$('#inputPartNumber');
            this.$inputPart = this.$('#inputPart');
            this.$inputPartName = this.$('#inputPartName');
            this.$inputProductId = this.$('#inputProductId');
            this.$inputDescription = this.$('#inputDescription');
        },

        bindTypeahead: function () {
            var map = {};
            var that = this;
            this.$inputPart.typeahead({
                source: function (query, process) {
                    var partNumbers = [];

                    $.getJSON(App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/numbers?q=' + query, function (data) {
                        _(data).each(function (d) {
                            var label = d.partName + ' < ' + d.partNumber + ' >';
                            partNumbers.push(label);
                            map[label] = d;
                        });
                        process(partNumbers);
                    });
                },
                updater: function(item) {
                    that.$inputPartName.val(map[item].partName);
                    that.$inputPartNumber.val(map[item].partNumber);
                    return item;
                }
            });
        },

        interceptSubmit : function(){
            this.isValid = ! this.$('.tabs').invalidFormTabSwitcher();
        },

        onSubmitForm: function (e) {

            this.$notifications.empty();

            if(this.isValid){
                this.model = new ConfigurationItem({
                    id: this.$inputProductId.val(),
                    workspaceId: App.config.workspaceId,
                    description: this.$inputDescription.val(),
                    designItemNumber: this.$inputPartNumber.val(),
                    designItemName: this.$inputPartName.val()
                });

                if (!this.model.getDesignItemNumber()) {
                    this.onError(App.config.i18n.PART_NOT_FOUND);
                    return false;
                }

                this.model.save({}, {
                    wait: true,
                    success: this.onProductCreated,
                    error: this.onError
                });
                this.model.fetch();
            }

            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        onProductCreated: function () {
            this.trigger('product:created', this.model);
            this.closeModal();
        },

        onError: function (model, error) {
            var errorMessage = error ? error.responseText : model;

            this.$notifications.append(new AlertView({
                type: 'error',
                message: errorMessage
            }).render().$el);
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

    return ProductCreationView;

});
