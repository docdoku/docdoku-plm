/*global ChannelListener,ChannelMessagesType,mainChannel*/
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

            if(APP_CONFIG.login!=message.sender && message.message.match(/^\//)){
               chatListener.handleCommand(message);
            }

            Backbone.Events.trigger('NewChatMessage', message);
        },

        // onStatusChangedHandler
        onStatusChanged: function (status) {
            Backbone.Events.trigger('ChatStatusChanged', status);
        }

    });

    chatListener.handleCommand = function(message){

        if(message.message.match(/inviteScene/)){
            //var url = message.message.substr(0 ,13);
            //App.sceneManager.handleInvite(message);
            //Backbone.Events.trigger('NewChatMessage', url);
        }else if(message.message.match(/animate/)){
            var contextString = message.message.substring(9);
            var context = JSON.parse(contextString);
            console.log(context);
            App.sceneManager.setControlsContext(context);
        } else if(message.message.match(/stop/)){
            App.appView.leaveSpectatorView();
            App.sceneManager.stopCollaborativeUsers();
        }
    };


    mainChannel.addChannelListener(chatListener);

});


