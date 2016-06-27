/*global define,require,App,_*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/linked/linked_document.html',
    'common-objects/models/document/document_revision'
], function (Backbone, Mustache, template, DocumentRevision) {
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
            var documentRevision = new DocumentRevision({
                id: this.model.get('documentMasterId') + '-' + this.model.get('version')
            });

            var self = this;

            documentRevision.fetch().success(function () {
                require(['common-objects/views/document/document_iteration'], function (IterationView) {
                    var view = new IterationView({
                        model: documentRevision,
                        iteration: self.model.getIteration ? self.model.getIteration() : undefined
                    });
                    view.show();
                });

            });
        }

    });
    return LinkedDocumentView;
});
