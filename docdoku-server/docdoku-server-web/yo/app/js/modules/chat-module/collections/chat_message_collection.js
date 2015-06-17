/*global define*/
define(
    ['backbone', 'modules/chat-module/models/chat_message_model'],
    function (Backbone, ChatMessage) {

        'use strict';

        var ChatMessageCollection = Backbone.Collection.extend({

            model: ChatMessage,

            getMessage: function () {
                return this.get('message');
            },

            getSender: function () {
                return this.get('sender');
            },

            getContext: function () {
                return this.get('context');
            },

            getRemoteUser: function () {
                return this.get('remoteUser');
            },

            getDate: function () {
                return this.get('date');
            }

        });

        return ChatMessageCollection;

    });
