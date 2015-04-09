/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/query_builder_modal.html',
], function (Backbone, Mustache, template) {
    'use strict';
    var QueryBuilderModal = Backbone.View.extend({

        events: {
            'hidden #query-builder-modal': 'onHidden'
        },

        initialize: function () {
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.bindDomElements();

            this.$('#builder').queryBuilder({
                filters: []
            });
            return this;
        },

        bindDomElements: function () {
            this.$modal = this.$('#query-builder-modal');
        },

        openModal: function () {
            this.$modal.modal('show');
        },

        closeModal: function () {
            this.$modal.modal('hide');
        },

        onHidden: function () {
            this.remove();
        },


    });



    return QueryBuilderModal
});
