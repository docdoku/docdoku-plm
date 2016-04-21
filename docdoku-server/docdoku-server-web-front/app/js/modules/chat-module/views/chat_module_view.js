/*global _,define*/
define(['backbone', 'modules/chat-module/views/chat_session_view'], function (Backbone, ChatMessageSessionView) {
	'use strict';
    var ChatModuleView = Backbone.View.extend({

        el: '#chat_module',

        initialize: function () {
            _.bindAll(this);
            this._chatSessionViews = [];
        },

        render: function () {
            // nothing to do
            return this;
        },

        onNewChatMessage: function (message) {
            var view = this.getChatSessionView(message);
            view.onNewChatMessage(message);
            // set above others
            Backbone.Events.trigger('ChatSessionFocusRequired', view);
        },

        onNewChatSession: function (chatSessionArgs) {
            var csv = this.getChatSessionView(chatSessionArgs);
            // set above others
            Backbone.Events.trigger('ChatSessionFocusRequired', csv);
        },

        onChatStatusChanged: function () {
        },

        getChatSessionView: function (chatSessionArgs) {

            // find or create the chat session view
            var sessionChatView = this.findOrCreateChatSessionViewForRemoteUser(chatSessionArgs);

            // set or update context and user
            sessionChatView.setRemoteUser(chatSessionArgs.remoteUser).setContext(chatSessionArgs.context);

            // render if not already done
            if (!sessionChatView._rendered) {
                this.$el.append(sessionChatView.render().$el);
            } else {
                sessionChatView.refreshTitle().$el.show();
            }
            // then return the session view
            return sessionChatView;

        },

        findOrCreateChatSessionViewForRemoteUser: function (chatSessionArgs) {

            // find the view of given remoteUser
            var view = _(this._chatSessionViews).select(function (view) {
                return view.remoteUser === chatSessionArgs.remoteUser;
            })[0];

            // create the view if it doesn't exist
            if (!view) {
                view = this.createNewChatSessionView();
            }

            // return the view
            return view;

        },

        createNewChatSessionView: function () {

            // create new a instance of ChatMessageSessionView
            var view = new ChatMessageSessionView();

            // push the view in memory
            this._chatSessionViews.push(view);

            // return the view
            return view;
        },

        onChatSessionFocusRequired: function (view) {

            _.each(this._chatSessionViews, function (csv) {
                csv.removeFromTop();
            });
            view.setOnTop();
        },

        onNewWebRTCInvitation: function (webRTCInvitation) {
            var csv = this.getChatSessionView(webRTCInvitation.message);
            csv.onWebRTCInvitation(webRTCInvitation);
        },

        onRemoveWebRTCInvitation: function (webRTCInvitation) {
            var csv = this.getChatSessionView(webRTCInvitation.message);
            if (csv && csv.webRTCInvitation !== null) {
                clearTimeout(csv.webRTCInvitation.invitationTimeout);
                csv.hideWebRTCInvitation();
            }
        }

    });

    return ChatModuleView;

});
