/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/product/product_details.html',
    'views/baselines/baseline_list',
    'views/baselines/baseline_detail_view',
    'common-objects/views/alert'
], function (Backbone, Mustache, template, BaselineListView, BaselineDetailView, AlertView) {

    'use strict';

    var ProductDetailsView = Backbone.View.extend({

        events: {
            'submit #product_details_form': 'onSubmitForm',
            'hidden #product_details_modal': 'onHidden',
            'close-modal-request':'closeModal'
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
            this.$tabs = this.$('.nav-tabs li');
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

            else{
                _this.closeModal();
            }
            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        initBaselinesView: function () {
            this.baselineListView = new BaselineListView({}, {productId: this.model.getId()}).render();
            this.$tabBaselines.append(this.baselineListView.$el);
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
        },

        activateTab: function (index) {
            this.$tabs.eq(index).children().tab('show');
        }

    });

    return ProductDetailsView;

});
