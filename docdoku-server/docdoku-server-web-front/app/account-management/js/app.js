/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/content.html',
    'views/edit-account'
], function (Backbone, Mustache, template, EditAccountView) {
	'use strict';
    var AppView = Backbone.View.extend({

        el: '#content',

        events: {
        },

        initialize: function () {
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n
            })).show();
            return this;
        },

        editAccount:function(){
            var view = new EditAccountView();
            this.$('#account-management-content').html(view.render().el);
        }

    });

    return AppView;
});
