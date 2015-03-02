/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/baselines/baseline_edit.html',
    'views/baselines/baselined_part_list',
    'common-objects/views/alert'
], function (Backbone, Mustache, template, BaselinePartListView,AlertView) {
    'use strict';
    var BaselineEditView = Backbone.View.extend({

        events: {
            'submit #baseline_edit_form': 'onSubmitForm',
            'hidden #baseline_edit_modal': 'onHidden'
        },

        initialize: function () {
            this.productId = this.options.productId;
        },

        render: function () {
            var that = this;
            this.model.fetch().success(function () {
                that.$el.html(Mustache.render(template, {i18n: App.config.i18n, model: that.model}));
                that.bindDomElements();
                that.initBaselinedPartListView();
                that.openModal();
            });
            window.document.body.appendChild(this.el);
            return this;
        },

        bindDomElements: function () {
            this.$notifications = this.$('.notifications');
            this.$modal = this.$('#baseline_edit_modal');
            this.$baselinedPartListArea = this.$('#baselinedPartListArea');
        },

        initBaselinedPartListView: function () {
            this.baselinePartListView = new BaselinePartListView({model: this.model, isForBaseline: true}).render();
            this.$baselinedPartListArea.html(this.baselinePartListView.$el);
        },

        onSubmitForm: function (e) {
            var _this = this;
            this.model.save({baselinedParts: this.baselinePartListView.getBaselinedParts()}, {
                success: function () {
                    _this.closeModal();
                },
                error: _this.onError.bind(_this)
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

    return BaselineEditView;
});
