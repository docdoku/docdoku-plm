/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/baselines/baseline_detail.html',
    'views/baselines/document_revision_list',
    'common-objects/utils/date'
], function (Backbone, Mustache, template, DocumentRevisionListView, date) {
    'use strict';
    var BaselineDetailView = Backbone.View.extend({

        events: {
            'hidden #baseline_detail_modal': 'onHidden',
            'close-modal-request': 'closeModal'
        },

        render: function () {
            var that = this;
            that.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                model: that.model
            }));
            that.bindDomElements();
            that.bindUserPopover();
            date.dateHelper(this.$('.date-popover'));
            that.displayDocumentRevisionListView();

            that.openModal();
            window.document.body.appendChild(this.el);
            return this;
        },

        bindDomElements: function () {
            this.$notifications = this.$('.notifications');
            this.$modal = this.$('#baseline_detail_modal');
            this.$tabs = this.$('.nav-tabs li');
        },

        bindUserPopover: function () {
            this.$('.author-popover').userPopover(this.model.getAuthorLogin(), App.config.i18n.BASELINE, 'left');
        },

        displayDocumentRevisionListView: function () {
            this.documentRevisionListView = new DocumentRevisionListView({editMode: false}).render();
            this.documentRevisionListView.renderList(this.model.getBaselinedDocuments());

            this.$('#documents-list').html(this.documentRevisionListView.el);
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
