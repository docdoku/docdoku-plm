/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/linked/linked_document.html',
    'common-objects/views/document/document_iteration',
    'common-objects/models/document/document'
], function (Backbone, Mustache, template, IterationView, Document) {
    'use strict';
	var LinkedDocumentView = Backbone.View.extend({

        tagName: 'li',
        className: 'linked-item well',

        events: {
            'click .delete-linked-item': 'deleteButtonClicked',
            'click .edit-linked-item-comment' : 'showEditCommentField',
            'click .delete-comment' : 'deleteComment',
            'click .validate-comment' : 'validateComment',
            'click a.reference': 'toDocumentDetailView'
        },

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.$el.html(Mustache.render(template,{
                i18n: App.config.i18n,
                linkedDocument: this.model,
                commentEditable: this.options.commentEditable,
                editMode: this.options.editMode
            }));

            return this;
        },

        deleteButtonClicked: function () {
            this.model.collection.remove(this.model);
            this.remove();
            return false;
        },

        showEditCommentField: function(){
            this.$el.toggleClass('edition');
        },

        deleteComment: function(){
            this.$('input.commentInput')[0].value = '';
        },

        validateComment:function(){
            var commentValue = this.$el.find('input.commentInput')[0].value;
            this.model.setDocumentLinkComment(commentValue);
            this.$('span.comment').html(commentValue);
            this.$el.toggleClass('edition');
        },

        toDocumentDetailView: function () {
            setTimeout(this.openDocumentDetailView, 500);
            this.$el.trigger('close-modal-request');
        },

        openDocumentDetailView: function () {
            var document = new Document({
                id: this.model.get("documentMasterId") + "-" + this.model.get("version")
            });

            document.fetch().success(function () {
                var view = new IterationView({
                    model: document
                });
                view.show();
            });
        }

    });
    return LinkedDocumentView;
});
