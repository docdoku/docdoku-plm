/*global define,App,_*/
define([
    'backbone',
    'mustache',
    'text!templates/typed-link-modal.html'
], function (Backbone, Mustache, template){
    'use strict';

    var TypedLinkModalView = Backbone.View.extend({

        className:'modal hide typed-link-modal',

        events: {
            'hidden': 'onHidden',
            'click .cancel-button': 'closeModal',
            'click .save-button': 'onSave'
        },

        initialize: function () {
            this.pathSelected = this.options.pathSelected;
        },

        bindDOMElements:function(){
            this.$modal = this.$el;
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n
            }));

            this.bindDOMElements();

            return this;
        },

        onSave: function(){

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

    return TypedLinkModalView;
});
