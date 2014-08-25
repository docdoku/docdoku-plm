/*global App,ChannelListener,ChannelMessagesType,mainChannel*/
define([
    "modules/chat-module/models/chat_message_model",
    "modules/chat-module/views/chat_module_view",
    "i18n!localization/nls/chat-module-strings"
],function (
    ChatMessage,
    ChatModuleView,
    i18n
    ) {

    var cmv = new ChatModuleView().render();

    // triggered on remote user message sent
    Backbone.Events.on('NewChatMessage', cmv.onNewChatMessage);

    // triggered on local user action
    Backbone.Events.on('NewChatSession', cmv.onNewChatSession);

    // triggered on websocket status changed
    Backbone.Events.on('ChatStatusChanged', cmv.onChatStatusChanged);

    // triggered on chat session requires focus
    Backbone.Events.on('ChatSessionFocusRequired', cmv.onChatSessionFocusRequired);

    // triggered on local user action
    Backbone.Events.on('NewWebRTCInvitation', cmv.onNewWebRTCInvitation);

    Backbone.Events.on('RemoveWebRTCInvitation', cmv.onRemoveWebRTCInvitation);

    var chatListener = new ChannelListener({

        // what kind of messages are we listening to ?
        isApplicable: function (messageType) {
            return messageType == ChannelMessagesType.CHAT_MESSAGE
                || messageType== ChannelMessagesType.CHAT_MESSAGE_ACK;
        },

        // onMessage handler
        onMessage: function (message) {

            if(APP_CONFIG.login!=message.sender && message.message.match(/^\/invite /)){
                var url = 'http://' + window.location.host + '/product-structure/' + message.message.substring(8);
                message.message = "<em>" + i18n.COLLABORATIVE_INVITE + " : </em>" + url;
            } else if(APP_CONFIG.login!=message.sender && message.message.match(/^\/withdrawInvitation/)){
                message.message = i18n.COLLABORATIVE_WITHDRAW_INVITATION;
            }

            Backbone.Events.trigger('NewChatMessage', message);
        },

        // onStatusChangedHandler
        onStatusChanged: function (status) {
            Backbone.Events.trigger('ChatStatusChanged', status);
        }

    });


    mainChannel.addChannelListener(chatListener);

});


