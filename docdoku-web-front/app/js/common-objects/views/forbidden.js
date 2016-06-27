/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/forbidden.html'
], function (Backbone, Mustache, template) {
	'use strict';
    var ForbiddenView = Backbone.View.extend({

        events: {
            'click .disconnect': 'disconnect',
            'click .back': 'back'
        },

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                title: App.config.i18n.FORBIDDEN,
                content:App.config.i18n.FORBIDDEN_MESSAGE,
                backButton: history.length > 2 ? App.config.i18n.HOME_PAGE:null,
                disconnectButton: App.config.i18n.LOGOUT
            }));
            this.bindDomElements();
            return this;
        },

        bindDomElements: function () {
            this.$modal = this.$('#forbidden_modal');
        },

        openModal: function () {
            this.$modal.modal('show');
        },

        closeModal: function () {
            this.$modal.modal('hide');
        },

        onShown: function () {
        },

        onHidden: function () {
            location.reload();
        },

        back:function(){
            window.location.href = App.config.contextPath + '/workspace-management/';
        },

        disconnect:function(){
            delete localStorage.jwt;
            $.get(App.config.contextPath + '/api/auth/logout').complete(function () {
                location.reload();
            });
        }

    });

    return ForbiddenView;
});
