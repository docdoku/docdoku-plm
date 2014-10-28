/*global define, App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/linked/linked_part.html'
], function (Backbone, Mustache, template) {
    'use strict';
	var LinkedPartView = Backbone.View.extend({

        tagName: 'li',
        className: 'linked-item well',

        events: {
            'click .delete-linked-item': 'deleteButtonClicked'
        },

        initialize: function () {
        },

        render: function () {
	        this.$el.html(Mustache.render(template,{
	            i18n: App.config.i18n,
	            linkedPart: this.model,
	            editMode: this.options.editMode
	        }));

            return this;
        },

        deleteButtonClicked: function () {
            this.model.collection.remove(this.model);
            this.remove();
            return false;
        }

    });
    return LinkedPartView;
});