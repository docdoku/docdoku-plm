define([
    "modules/chat-module/models/chat_message_model",
    "modules/chat-module/views/chat_module_view"
],function (
    ChatMessage,
    ChatModuleView
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

    var chatListener = new ChannelListener({

        // what kind of messages are we listening to ?
        isApplicable: function (messageType) {
            return messageType == ChannelMessagesType.CHAT_MESSAGE;
        },

        // onMessage handler
        onMessage: function (message) {
            Backbone.Events.trigger('NewChatMessage', message);
        },

        // onStatusChangedHandler
        onStatusChanged: function (status) {
            Backbone.Events.trigger('ChatStatusChanged', status);
        }

    });

    mainChannel.addChannelListener(chatListener);

});