define(function(){

    function CollaborativeController () {

        var collaborativeView = App.collaborativeView;

        var collaborativeListener = new ChannelListener({

            messagePattern: /^COLLABORATIVE_.+/,

            isApplicable: function (messageType) {
                return messageType.match(this.messagePattern) != null;
            },

            onMessage: function (message) {

                if (message.type == ChannelMessagesType.COLLABORATIVE_CREATE) {
                    collaborativeView.roomCreated(message.key, message.remoteUser);
                    return;
                }

                if (message.type == ChannelMessagesType.COLLABORATIVE_INFO) {
                    alert(message.messageBroadcast);
                    return;
                }

                if (message.type == ChannelMessagesType.COLLABORATIVE_JOIN && collaborativeView.roomKey == message.key) {
                    collaborativeView.onCallAcceptedByRemoteUser(message);
                    mainChannel.sendJSON({
                        type: ChannelMessagesType.COLLABORATIVE_INVITE,
                        roomKey: message.roomKey,
                        remoteUser: message.remoteUser,
                        reason: REJECT_CALL_REASON.BUSY
                    });
                    return;
                }

                if (message.type == ChannelMessagesType.COLLABORATIVE_CONTEXT && collaborativeView.roomKey == message.key) {

                    var jsonContext = JSON.parse(message.messageBroadcast);
                    console.log(jsonContext);
                    collaborativeView.setMaster(jsonContext.master);
                    collaborativeView.setUsers(jsonContext.users);
                    collaborativeView.setPendingUsers(jsonContext.pendingUsers);

                    //alert(message.messageBroadcast);
                    return;
                }

                if (message.type == ChannelMessagesType.COLLABORATIVE_KICK_USER && collaborativeView.roomKey == message.key) {
                    alert(message.messageBroadcast);
                    App.sceneManager.kickedFromCollaborative();
                    collaborativeView.reset();
                    return;
                }

                if (message.type == ChannelMessagesType.COLLABORATIVE_COMMANDS && collaborativeView.roomKey == message.key) {
                    var jsonContext = JSON.parse(message.messageBroadcast);
                    //console.log(jsonContext);
                    App.sceneManager.setControlsContext(jsonContext);
                    return;
                }

                if (message.type == ChannelMessagesType.COLLABORATIVE_GIVE_HAND && collaborativeView.roomKey == message.key) {
                    //collaborativeView.setMaster()
                    App.sceneManager.setCollaborativeMaster();
                    return;
                }
                //Backbone.Events.trigger('', message);
            },

            onStatusChanged: function (status) {
                Backbone.Events.trigger('ChatStatusChanged', status);
            }

        });

        mainChannel.addChannelListener(collaborativeListener);
    };

    /*
    CollaborativeController.prototype = {

    };*/

    return CollaborativeController;
});
