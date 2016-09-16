/*global _,$,define,App,window*/
define([
    'backbone',
    'mustache',
    'text!templates/baselines/document_revision_list.html',
    'common-objects/models/document/document_revision',
    'views/baselines/document_revision_list_item',
    'common-objects/views/alert'

], function (Backbone, Mustache, template, DocumentRevision, DocumentRevisionListItemView, AlertView) {
    'use strict';
    var DocumentsChoiceListView = Backbone.View.extend({

        tagName: 'div',

        className: 'documents-list',

        events: {
            'submit form':'formSubmit'
        },

        initialize: function () {
            _.bindAll(this);

            this.title = this.options.title;
            this.checkAll = true;

            this.documentRevisions = [];
            this.documentsViews = [];
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                title:this.title
            }));

            this.bindDomElements();
            return this;
        },

        bindDomElements: function () {
            this.$list = this.$('.documents');
            this.notifications = this.$('div.notifications');
        },

        renderList:function(documentRevisions){
            this.clear();
            this.documentRevisions = documentRevisions;
            _.each(this.documentRevisions, this.addDocumentItemView, this);
        },

        addDocumentItemView: function (documentRevision) {
            var view = new DocumentRevisionListItemView({model:documentRevision}).render();
            this.documentsViews.push(view);
            this.$list.append(view.$el);

            // TODO: use this
            this.listenTo(view,'notification', this.printNotifications);
            this.listenTo(view,'remove', this.removeItemView.bind(this));
        },

        removeItemView:function(view){
            this.documentsViews = _.without(this.documentsViews, view);
            var index = this.documentRevisions.indexOf(view.model);
            this.documentRevisions.splice(index, 1);
            view.$el.remove();
            this.trigger('update');
        },

        clear:function(){
            this.documentRevisions = [];
            this.documentsViews = [];
            this.removeSubviews();
            this.$list.empty();
        },

        removeSubviews: function () {
            _(this.documentsViews).invoke('remove');
        },

        formSubmit: function () {
            return false;
        },

        printNotifications: function(type,message) {
            this.notifications.append(new AlertView({
                type: type,
                message: message
            }).render().$el);
        },

        clearNotifications: function() {
            this.notifications.text('');
        }
    });

    return DocumentsChoiceListView;
});
