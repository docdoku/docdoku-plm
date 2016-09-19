/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/baselines/baseline_detail.html',
    'common-objects/utils/date'
], function (Backbone, Mustache, template, date) {
    'use strict';
    var BaselineDetailView = Backbone.View.extend({

        events: {
            'hidden #baseline_detail_modal': 'onHidden',
            'close-modal-request': 'closeModal'
        },

        initialize: function () {

        },

        render: function () {
            var that = this;
            that.$el.html(Mustache.render(template, {i18n: App.config.i18n, model: that.model}));
            that.bindDomElements();
            date.dateHelper(this.$('.date-popover'));
            that.openModal();

            window.document.body.appendChild(this.el);
            return this;
        },

        bindDomElements: function () {
            this.$notifications = this.$('.notifications');
            this.$modal = this.$('#baseline_detail_modal');
            this.$tabs = this.$('.nav-tabs li');
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
