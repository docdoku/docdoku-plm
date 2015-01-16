/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/udf/user_defined_function.html'
], function (Backbone, Mustache, template) {

    'use strict';

    var UserDefinedFunctionView = Backbone.View.extend({

        events: {
            'hidden #user_defined_function_modal': 'onHidden',
            'submit #user_defined_function_form':'run'
        },

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.$modal= this.$('#user_defined_function_modal');
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
        },

        run: function(e){

            alert('run')
            e.preventDefault();
            e.stopPropagation();
            return false;
        }

    });

    return UserDefinedFunctionView;

});
