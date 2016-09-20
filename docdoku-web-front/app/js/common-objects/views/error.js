/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/error.html'
], function (Backbone, Mustache, template) {
	'use strict';
    var ErrorView = Backbone.View.extend({

        events: {
            'click .disconnect': 'disconnect',
            'click .workspace-management': 'toWorkspaceManagement',
            'click .back': 'back'
        },

        initialize: function () {
            _.bindAll(this);
        },

        render: function (opts) {
            this.$el.html(Mustache.render(template, {
                title: opts.title,
                content: opts.content,
                i18n: App.config.i18n
            }));
            this.$el.addClass('error-page');
            return this;
        },

        toWorkspaceManagement:function(){
            window.location.href = App.config.contextPath + '/workspace-management/';
        },

        back:function(){
            window.history.back();
        },

        disconnect:function(){
            delete localStorage.jwt;
            $.get(App.config.contextPath + '/api/auth/logout').complete(function () {
                location.reload();
            });
        }

    });

    return ErrorView;
});
