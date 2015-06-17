/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/linked/linked_document.html'
], function (Backbone, Mustache, template) {
    'use strict';
	var LinkedDocumentView = Backbone.View.extend({

        tagName: 'li',
        className: 'linked-item well',

        events: {
            'click .delete-linked-item': 'deleteButtonClicked',
            'click .edit-linked-item-comment' : 'showEditCommentField',
            'click .delete-comment' : 'deleteComment',
            'click .validate-comment' : 'validateComment'
        },

        initialize: function () {
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
        }
    });
    return LinkedDocumentView;
});
