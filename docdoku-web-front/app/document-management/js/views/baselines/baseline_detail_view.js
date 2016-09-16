/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/baselines/baseline_detail.html',
    'common-objects/views/linked/linked_documents',
    'common-objects/collections/linked/linked_document_iteration_collection',
    'common-objects/utils/date'
], function (Backbone, Mustache, template, LinkedDocumentsView, LinkedDocumentIterationCollection, date) {
    'use strict';
    var BaselineDetailView = Backbone.View.extend({

        events: {
            'hidden #baseline_detail_modal': 'onHidden',
            'close-modal-request': 'closeModal'
        },

        render: function () {
            var that = this;
            that.$el.html(Mustache.render(template, {i18n: App.config.i18n, model: that.model}));
            that.bindDomElements();
            that.bindUserPopover();
            date.dateHelper(this.$('.date-popover'));
            that.displayLinkedDocumentsView();

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

        displayLinkedDocumentsView: function () {
            this.linkedDocumentsView = new LinkedDocumentsView({
                editMode: false,
                commentEditable:false,
                collection: new LinkedDocumentIterationCollection(this.model.getBaselinedDocuments())
            }).render();

            this.$('#documents-list').html(this.linkedDocumentsView.el);
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
