/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!modules/coworkers-access-module/templates/coworker_item_template.html'
], function (Backbone, Mustache, template) {
	'use strict';
    var CoWorkersItemView = Backbone.View.extend({
        tagName: 'li',

        events: {
            'click .fa-video-camera': 'onVideoButtonClick',
            'click .fa-comments': 'onChatButtonClick',
            'click .fa-envelope': 'onMailButtonClick',
            'click .fa-globe': 'onCobrowsingButtonClick'
        },

        initialize: function () {
            var data = {
                user: this.model.login
            };
            if (typeof(App) !== 'undefined' && App.sceneManager) {
                data.displayCobrowsingButton = this.model.workspaceId === App.config.workspaceId;
            }
            this.template = Mustache.render(template, data);
            _.bindAll(this);
            return this;
        },

        render: function () {
            this.$el.html(this.template);
            return this;
        },

        refreshAvailability: function () {
            var that = this;
            // Listen for the status request done
            Backbone.Events.on('UserStatusRequestDone', function (message) {
                if (message.remoteUser === that.model.login && message.status !== null) {
                    if (message.status === 'OFFLINE') {
                        that.$('.fa-user').addClass('user-offline').removeClass('user-online').attr('title', App.config.i18n.OFFLINE);
                    } else if (message.status === 'ONLINE') {
                        that.$('.fa-user').addClass('user-online').removeClass('user-offline').attr('title', App.config.i18n.ONLINE);
                    }
                }
            });
            // trigger the status request
            Backbone.Events.trigger('UserStatusRequest', that.model.login);
        },

        onVideoButtonClick: function () {
            Backbone.Events.trigger('NewOutgoingCall', { remoteUser: this.model.login, context: App.config.workspaceId });
        },

        onChatButtonClick: function () {
            Backbone.Events.trigger('NewChatSession', { remoteUser: this.model.login, context: App.config.workspaceId });
        },

        onMailButtonClick: function () {
            window.location.href = encodeURI('mailto:' + this.model.email + '?subject=' + App.config.workspaceId);
        },

        onCobrowsingButtonClick: function () {
            Backbone.Events.trigger('SendCollaborativeInvite', this.model.login);
        }
    });

    return CoWorkersItemView;
});
