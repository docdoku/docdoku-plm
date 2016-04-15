/*global $,define,App*/
define([
    'backbone',
    'common-objects/utils/date',
    'common-objects/views/documents/checkbox_list_item',
    'common-objects/views/document/document_iteration',
    'text!templates/document_list_item.html',
    'common-objects/views/share/share_entity'
], function (Backbone, date, CheckboxListItemView, IterationView, template, ShareView) {
    'use strict';
    var DocumentListItemView = CheckboxListItemView.extend({

        template: template,

        tagName: 'tr',

        initialize: function () {
            CheckboxListItemView.prototype.initialize.apply(this, arguments);

            // jQuery creates it's own event object, and it doesn't have a
            // dataTransfer property yet. This adds dataTransfer to the event object.
            $.event.props.push('dataTransfer');

            this.events['click .reference'] = this.actionEdit;
            this.events['click .state-subscription'] = this.toggleStateSubscription;
            this.events['click .iteration-subscription'] = this.toggleIterationSubscription;
            this.events['click .document-master-share i'] = this.shareDocument;
            this.events['click .document-attached-files i'] = this.openDocumentModal;
            this.events['dragstart a.dochandle'] = this.dragStart;
            this.events['dragend a.dochandle'] = this.dragEnd;
            this.events['dragstart td.doc-ref'] = this.dragStart;
            this.events['dragend td.doc-ref'] = this.dragEnd;

        },

        modelToJSON: function () {

            var data = this.model.toJSON();
            data.reference = this.model.getReference();

            if (this.model.hasIterations()) {
                data.lastIteration = this.model.getLastIteration().toJSON();
                data.lastIteration.modificationDate = date.formatTimestamp(
                    App.config.i18n._DATE_FORMAT,
                    data.lastIteration.modificationDate
                );
            }

            if (this.model.hasACLForCurrentUser()) {
                data.isReadOnly = this.model.isReadOnly();
                data.isFullAccess = this.model.isFullAccess();
            }

            data.isCheckoutByConnectedUser = this.model.isCheckoutByConnectedUser();
            data.isCheckout = this.model.isCheckout();
            if (this.model.getLastIteration()){
                data.hasAttachedFiles = this.model.getLastIteration().getAttachedFiles().length;
            }

            return data;
        },

        rendered: function () {
            CheckboxListItemView.prototype.rendered.apply(this, arguments);

            if (this.model.isStateChangedSubscribed()) {
                this.$('.state-subscription').addClass('fa-bell').attr('title', App.config.i18n.UNSUBSCRIBE_STATE_CHANGE);
            } else {
                this.$('.state-subscription').addClass('fa-bell-o').attr('title', App.config.i18n.SUBSCRIBE_STATE_CHANGE);
            }

            if (this.model.isIterationChangedSubscribed()) {
                this.$('.iteration-subscription').addClass('fa-bell').attr('title', App.config.i18n.UNSUBSCRIBE_ITERATION_CHANGE);
            } else {
                this.$('.iteration-subscription').addClass('fa-bell-o').attr('title', App.config.i18n.SUBSCRIBE_ITERATION_CHANGE);
            }

            this.$('.author-popover').userPopover(this.model.attributes.author.login, this.model.id, 'left');

            if (this.model.isCheckout()) {
                this.$('.checkout-user-popover').userPopover(this.model.getCheckoutUser().login, this.model.id, 'left');
            }

            date.dateHelper(this.$('.date-popover'));
            this.bindDescriptionPopover();
            this.$el.attr('title',this.model.getReference());

        },

        bindDescriptionPopover: function() {
            if(this.model.getDescription() !== null && this.model.getDescription() !== '') {
                var self = this;
                this.$('.reference.doc-ref')
                    .popover({
                        title: App.config.i18n.DESCRIPTION,
                        html: true,
                        content: self.model.getDescription(),
                        trigger: 'hover',
                        placement: 'top',
                        container: 'body'
                    });
            }
        },

        dragStart: function (e) {
            var that = this;
            this.$el.addClass('moving');

            Backbone.Events.on('document-moved', function () {
                Backbone.Events.off('document-moved');
                Backbone.Events.off('document-error-moved');
                that.model.collection.remove(that.model);
            });
            Backbone.Events.on('document-error-moved', function () {
                Backbone.Events.off('document-moved');
                Backbone.Events.off('document-error-moved');
                that.$el.removeClass('moving');
            });
            var data = JSON.stringify(this.model.toJSON());
            var img = document.createElement('img');
            img.src = App.config.contextPath + '/images/icon-action-document-move.png';
            e.dataTransfer.setDragImage(img, 0, 0);
            e.dataTransfer.setData('document:text/plain', data);
            e.dataTransfer.dropEffect = 'none';
            e.dataTransfer.effectAllowed = 'copyMove';
            return e;
        },

        dragEnd: function (e) {
            if (e.dataTransfer.dropEffect === 'none') {
                Backbone.Events.off('document-moved');
                Backbone.Events.off('document-error-moved');
            }
            this.$el.removeClass('moving');
        },

        actionEdit: function () {
            var that = this;
            this.model.fetch().success(function () {
                new IterationView({
                    model: that.model
                }).show();
            });
        },

        openDocumentModal: function(){
            var that = this;
            this.model.fetch().success(function () {
                var view = new IterationView({
                    model: that.model
                });
                view.show();
                view.activateFileTab();

            });

        },
        toggleStateSubscription: function () {
            this.model.toggleStateSubscribe(this.model.isStateChangedSubscribed());
        },

        toggleIterationSubscription: function () {
            this.model.toggleIterationSubscribe(this.model.isIterationChangedSubscribed());
        },

        shareDocument: function () {
            var that = this;
            var shareView = new ShareView({model: that.model, entityType: 'documents'});
            window.document.body.appendChild(shareView.render().el);
            shareView.openModal();

        }

    });

    return DocumentListItemView;
});
