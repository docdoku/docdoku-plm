/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/baselines/document_revision_list_item.html',
    'common-objects/views/part/part_modal_view',
    'common-objects/models/document/document_revision'
], function (Backbone, Mustache, template, PartModalView, DocumentRevision) {
    'use strict';
    var DocumentRevisionListItemView = Backbone.View.extend({

        tagName: 'div',

        className: 'control-group',

        events: {
            'click .remove': 'remove'
        },

        template: Mustache.parse(template),

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                model: this.model,
                i18n: App.config.i18n
            }));

            return this;
        },

        remove: function() {
            this.trigger('remove', this);
        }
    });

    return DocumentRevisionListItemView;
});
