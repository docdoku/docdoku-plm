/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/baselines/baseline_detail.html',
    'views/baselines/baselined_part_list'
], function (Backbone, Mustache, template, BaselinePartListView) {
    'use strict';
    var BaselineDetailView = Backbone.View.extend({

        events: {
            'hidden #baseline_detail_modal': 'onHidden'
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
            this.$modal = this.$('#baseline_detail_modal');
            this.$baselinedPartListArea = this.$('.baselinedPartListArea');
        },

        initBaselinedPartListView: function () {
            this.baselinePartListView = new BaselinePartListView({
                model: this.model,
                editMode:false
            }).render();
            this.$baselinedPartListArea.html(this.baselinePartListView.$el);
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

    return BaselineDetailView;
});
