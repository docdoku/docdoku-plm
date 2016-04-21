/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!modules/chat-module/templates/chat_message_view.html',
    'modules/chat-module/collections/chat_message_collection',
    'modules/chat-module/views/chat_message_view',
    'common-objects/websocket/channelMessagesType'
], function (Backbone, Mustache, template, ChatMessageCollection, ChatMessageView, ChannelMessagesType) {
    'use strict';
    var ChatMessageSessionView = Backbone.View.extend({

        events: {
            'submit form': 'onSubmitForm',
            'click .fa-times': 'onClose',
            'click .fa-video-camera': 'onVideoButtonClick',
            'click .chat_session_title': 'onGlobalClick',
            'click .chat_session_messages': 'onGlobalClick',
            'click input[name=chat_message_session_input]': 'onGlobalClick',
            'click a.accept-webrtc': 'onWebRTCAccept',
            'click a.reject-webrtc': 'onWebRTCReject'
        },

        className:'chat_session',

        initialize: function () {

            this.isOnTop = false;

            this._messagesViews = [];

            this.collection = new ChatMessageCollection();

            this.collection.bind('add', this.add, this);

            _(this).bindAll('onSubmitForm', 'onClose', 'onGlobalClick', 'onVideoButtonClick');

        },

        add: function (chatMessage) {

            var that = this;

            var cmv = new ChatMessageView({
                model: chatMessage
            });

            this._messagesViews.push(cmv);

            if (this._rendered) {
                this.$el.show();

                // append new message
                var $ul = this.$('ul.chat_session_messages');
                $ul.append(cmv.render().el);
                $ul[0].scrollTop = $ul[0].scrollHeight;

                // stick to bottom if needed
                var thatElHeight = that.$el.height();
                var offsetTop = that.$el.offset().top;
                var windowHeight = window.innerHeight;

                if (windowHeight - thatElHeight - offsetTop < 15) {
                    this.$el.css({'bottom': '0', 'top': 'auto'});
                }
            }

        },

        refreshTitle: function () {
            this.$('.chat_session_title').html('<i class="fa fa-envelope"></i> ' + this.remoteUser + ' | ' + this.context);
            return this;
        },

        onNewChatMessage: function (message) {
            if (message.error) {
                this.onError(message);
            } else {
                this.collection.push(message);

                if (message.sender !== App.config.login) {
                    Backbone.Events.trigger('NotificationSound');
                }

            }
            return this;
        },

        setRemoteUser: function (remoteUser) {
            this.remoteUser = remoteUser;
            return this;
        },

        setContext: function (context) {
            this.context = context;
            return this;
        },

        render: function () {

            var that = this;

            this.$el.html(Mustache.render(template, {
                chatSession: {
                    remoteUser: this.remoteUser,
                    context: this.context
                },
                i18n: App.config.i18n
            }));

            this._rendered = true;
            this.delegateEvents();

            this.$el.draggable({
                handle: 'div.chat_session_header',
                containment: 'body',
                position: 'absolute',
                addClasses: false,
                start: function () {
                    that.$el.css('bottom', 'auto');
                }
            });

            // override jquery style, better behavior
            this.$el.css('position', 'absolute');

            this.$WebRTCInvitation = this.$('.chat_webrtc_invite');

            return this;
        },

        onSubmitForm: function (event) {

            if (!App.mainChannel.isReady()) {
                this.$('ul.chat_session_messages').append('<li class="chat_message_error">' + App.config.i18n.ERROR + ' : ' + App.config.i18n.CHANNEL_NOT_READY_ERROR + '</li>');
                event.stopPropagation();
                event.preventDefault();
                return false;
            }

            var textInput = event.target.children[0];

            if (!textInput.value.length) {
                return false;
            }

            // build the message
            var message = {
                type: ChannelMessagesType.CHAT_MESSAGE,
                remoteUser: this.remoteUser,
                message: textInput.value,
                context: this.context,
                sender: App.config.login
            };

            // send it to remote
            App.mainChannel.sendJSON(message);

            // trigger message render on local
            //Backbone.Events.trigger('NewChatMessage', message);

            textInput.value = '';

            event.stopPropagation();
            event.preventDefault();
            return false;

        },

        onClose: function () {
            this.$el.hide();
        },

        onError: function (message) {
            if (this._rendered) {
                this.$('ul.chat_session_messages').append('<li class="chat_message_error">' + App.config.i18n.ERROR + ' : ' + App.config.i18n[message.error] + '</li>');
                this.$el.show();
            }
        },

        focusInput: function () {
            this.$('input[name=chat_message_session_input]').focus();
            return this;
        },

        onGlobalClick: function () {
            if (!this.isOnTop) {
                Backbone.Events.trigger('ChatSessionFocusRequired', this);
            }
        },

        onVideoButtonClick: function () {
            Backbone.Events.trigger('NewOutgoingCall', {remoteUser: this.remoteUser, context: this.context});
        },

        setOnTop: function () {
            this.$el.addClass('chat_session_on_top');
            this.isOnTop = true;
            this.focusInput();
            return this;
        },

        removeFromTop: function () {
            this.$el.removeClass('chat_session_on_top');
            this.isOnTop = false;
            return this;
        },

        onWebRTCInvitation: function (webRTCInvitation) {
            this.webRTCInvitation = webRTCInvitation;
            this.$WebRTCInvitation.show();
        },

        onWebRTCAccept: function () {
            if (this.webRTCInvitation) {
                this.webRTCInvitation.accept();
                this.webRTCInvitation = null;
                this.hideWebRTCInvitation();
            }
        },

        onWebRTCReject: function () {
            if (this.webRTCInvitation) {
                this.webRTCInvitation.reject();
                this.webRTCInvitation = null;
                this.hideWebRTCInvitation();
            }
        },

        hideWebRTCInvitation: function () {
            this.$WebRTCInvitation.hide();
        }

    });

    return ChatMessageSessionView;
});
