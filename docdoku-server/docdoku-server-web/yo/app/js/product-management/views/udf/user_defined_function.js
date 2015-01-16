/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/udf/user_defined_function.html'
], function (Backbone, Mustache, template) {

    'use strict';

    var UserDefinedFunctionView = Backbone.View.extend({

        events: {
            'hidden #advanced_search_modal': 'onHidden'
        },

        initialize: function () {
            _.bindAll(this);

        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            return this;
        },

        openModal: function () {
            this.$modal.modal('show');
        },

        closeModal: function () {
            this.$modal.modal('hide');
        },

        onHidden: function () {
            this.remove();
        }


    });

    return UserDefinedFunctionView;

});
