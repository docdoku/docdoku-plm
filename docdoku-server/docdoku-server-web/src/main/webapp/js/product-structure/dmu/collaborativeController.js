/*global App,ChannelListener,ChannelMessagesType,mainChannel*/
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

                if (message.type == ChannelMessagesType.COLLABORATIVE_JOIN && collaborativeView.roomKey == message.key) {
                    collaborativeView.onCallAcceptedByRemoteUser(message);
                    mainChannel.sendJSON({
                        type: ChannelMessagesType.COLLABORATIVE_INVITE,
                        roomKey: message.roomKey,
                        remoteUser: message.remoteUser
                    });
                    return;
                }

                if (message.type == ChannelMessagesType.COLLABORATIVE_CONTEXT && collaborativeView.roomKey == message.key) {
                    collaborativeView.setMaster(message.messageBroadcast.master);
                    collaborativeView.setUsers(message.messageBroadcast.users);
                    collaborativeView.setPendingUsers(message.messageBroadcast.pendingUsers);
                    return;
                }

                if (message.type == ChannelMessagesType.COLLABORATIVE_KICK_USER && collaborativeView.roomKey == message.key) {
                    alert("You've been kicked");
                    App.appView.leaveSpectatorView();
                    App.sceneManager.enableControlsObject();
                    collaborativeView.reset();
                    return;
                }

                if (message.type == ChannelMessagesType.COLLABORATIVE_KICK_NOT_INVITED && collaborativeView.roomKey == message.key) {
                    alert("You are not invited to join this room.");
                    App.appView.leaveSpectatorView();
                    App.sceneManager.enableControlsObject();
                    collaborativeView.reset();
                    return;
                }

                if (message.type == ChannelMessagesType.COLLABORATIVE_COMMANDS && collaborativeView.roomKey == message.key) {
                    if(message.messageBroadcast.contextInfos) {
                        App.sceneManager.setControlsContext(message.messageBroadcast.contextInfos);
                    } else if(message.messageBroadcast.instancesId) {
                        console.log(message.messageBroadcast.instancesId);
                        App.instancesManager.loadFromId(message.messageBroadcast.instancesId);
                    } else if (message.messageBroadcast.instancesUnChecked){
                        App.instancesManager.unloadFromId(message.messageBroadcast.instancesUnChecked);
                    }
                    return;
                }

                if (message.type == ChannelMessagesType.COLLABORATIVE_GIVE_HAND && collaborativeView.roomKey == message.key) {
                    App.appView.leaveSpectatorView();
                    App.sceneManager.enableControlsObject();
                }
            },

            onStatusChanged: function (status) {
                Backbone.Events.trigger('ChatStatusChanged', status);
            }

        });

        mainChannel.addChannelListener(collaborativeListener);
    }

    return CollaborativeController;
});
