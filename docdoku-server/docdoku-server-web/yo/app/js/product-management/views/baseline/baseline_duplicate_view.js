/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/baseline/baseline_duplicate.html',
    'common-objects/views/alert'
], function (Backbone, Mustache, template,AlertView) {
    'use strict';
    var BaselineDuplicateView = Backbone.View.extend({
        events: {
            'submit #baseline_duplicate_form': 'onSubmit',
            'hidden #baseline_duplicate_modal': 'onHidden'
        },

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.bindDomElements();
            this.initValue();
            return this;
        },

        bindDomElements: function () {
            this.$notifications = this.$el.find('.notifications').first();
            this.$modal = this.$('#baseline_duplicate_modal');
            this.$inputBaselineName = this.$('#inputBaselineName');
            this.$inputBaselineType = this.$('#inputBaselineType');
            this.$inputBaselineDescription = this.$('#inputBaselineDescription');
        },

        initValue: function () {
            this.$inputBaselineType.val(this.model.getType());
            this.$inputBaselineDescription.val(this.model.getDescription());
        },

        onSubmit: function (e) {
            var _this = this;
            var data = {
                name: this.$inputBaselineName.val(),
                type: this.model.getType(),
                description: this.$inputBaselineDescription.val()
            };

            this.model.duplicate({
                data: data,
                success: function (baseline) {
                    _this.closeModal();
                    _this.model = baseline;
                },
                error: _this.onError
            });

            e.preventDefault();
            e.stopPropagation();
            return false;

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

    return BaselineDuplicateView;
});
