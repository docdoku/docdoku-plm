/*global define,App */
define([
    'backbone',
    'mustache',
    'text!templates/product/product_details.html',
    'views/baseline/baseline_list',
    'views/baseline/baseline_edit_view',
    'common-objects/views/alert'
], function (Backbone, Mustache, template, BaselineListView, BaselineEditView, AlertView) {
    'use strict';
    var ProductDetailsView = Backbone.View.extend({

        events: {
            'submit #product_details_form': 'onSubmitForm',
            'hidden #product_details_modal': 'onHidden'
        },

        template: Mustache.parse(template),

        initialize: function () {

        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n, model: this.model}));
            this.bindDomElements();
            this.initBaselinesView();
            return this;
        },

        bindDomElements: function () {
            this.$notifications = this.$el.find('.notifications').first();
            this.$modal = this.$('#product_details_modal');
            this.$tabBaselines = this.$('#tab-baselines');
        },

        onSubmitForm: function (e) {
            var _this = this;
            var baselines = this.baselineListView.getCheckedBaselines();

            if (baselines.length) {
                this.model.deleteBaselines(baselines,{
                    success: _this.closeModal.bind(this),
                    error: _this.onError.bind(this)
                });
            }

            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        initBaselinesView: function () {
            var that = this;
            this.baselineListView = new BaselineListView({}, {productId: this.model.getId()}).render();
            this.$tabBaselines.append(this.baselineListView.$el);
            this.listenToOnce(this.baselineListView, 'baseline:to-edit-modal', function (baseline) {
                that.closeModal();
                new BaselineEditView({model: baseline}, {productId: that.model.getId()}).render();
            });
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

    return ProductDetailsView;

});
