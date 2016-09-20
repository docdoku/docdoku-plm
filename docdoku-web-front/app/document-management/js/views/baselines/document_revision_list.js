/*global _,$,define,App,window*/
define([
    'backbone',
    'mustache',
    'text!templates/baselines/document_revision_list.html',
    'common-objects/models/document/document_revision',
    'views/baselines/document_revision_list_item'

], function (Backbone, Mustache, template, DocumentRevision, DocumentRevisionListItemView) {
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
            this.editMode = this.options.editMode;
            this.checkAll = true;

            this.documentRevisions = [];
            this.documentsViews = [];
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                title: this.title
            }));

            this.bindDomElements();
            return this;
        },

        bindDomElements: function () {
            this.$list = this.$('.documents');
        },

        renderList:function(documentRevisions){
            this.clear();
            this.documentRevisions = documentRevisions;
            _.each(this.documentRevisions, this.addDocumentItemView, this);
        },

        addDocumentItemView: function (documentRevision) {

            var multipleVersions = false;
            _.each(this.documentRevisions, function (otherDocumentRevision) {
                if (documentRevision.getId() !== otherDocumentRevision.getId() && documentRevision.getReference() === otherDocumentRevision.getReference()) {
                    multipleVersions = true;
                    return;
                }
            }, this);

            var view = new DocumentRevisionListItemView({
                model:documentRevision,
                editMode: this.editMode,
                multiple: multipleVersions
            }).render();

            this.documentsViews.push(view);
            this.$list.append(view.$el);

            this.listenTo(view, 'remove', this.removeItemView.bind(this));
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
        }
    });

    return DocumentsChoiceListView;
});
