/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/tags/tag.html'
], function (Backbone, Mustache, template) {
    'use strict';
    var TagView = Backbone.View.extend({
        tagName: 'li',
        className: 'pull-left well',

        initialize: function () {
            if (this.options.isAdded) {
                this.events = { 'click a': 'clicked' };
            }
            else if (this.options.isAvailable) {
                this.events = { 'click a': 'crossClicked',
                    'click': 'clicked' };
            } else {
                this.events = { 'click': 'clicked' };
            }

            this.isRemovable = (this.options.isAdded || this.options.isAvailable) && !App.config.isReadOnly;

            return this;
        },

        render: function () {
            this.$el.html(Mustache.render(template, {tag: this.model, isRemovable: this.isRemovable}));
            return this;
        },

        clicked: function () {
            if (this.options.clicked) {
                this.options.clicked();
            }
        },

        crossClicked: function (e) {
            if (this.options.crossClicked) {
                this.options.crossClicked();
            }
            e.stopPropagation();
        }

    });

    return TagView;
});
