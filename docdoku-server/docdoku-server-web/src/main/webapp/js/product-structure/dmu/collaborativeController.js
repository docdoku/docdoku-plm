/*global App,ChannelListener,ChannelMessagesType,mainChannel*/
define(function(){

    function CollaborativeController () {

        var collaborativeView = App.collaborativeView;

        var collaborativeListener = new ChannelListener({

            messagePattern: /^COLLABORATIVE_.+/,

            isApplicable: function (messageType) {
                return messageType.match(this.messagePattern) !== null;
            },

            onMessage: function (message) {

                if (message.type === ChannelMessagesType.COLLABORATIVE_CREATE) {
                    collaborativeView.roomCreated(message.key, message.remoteUser);
                    return;
                }

                if (message.type === ChannelMessagesType.COLLABORATIVE_JOIN) {
                    App.sceneManager.joinRoom(message.key);
                    return;
                }

                if (message.type === ChannelMessagesType.COLLABORATIVE_CONTEXT && collaborativeView.roomKey === message.key) {
                    if (message.messageBroadcast.master === ''){
                        collaborativeView.setMaster("nobody");
                    } else {
                        collaborativeView.setMaster(message.messageBroadcast.master);
                    }
                    collaborativeView.setUsers(message.messageBroadcast.users);
                    collaborativeView.setPendingUsers(message.messageBroadcast.pendingUsers);
                    return;
                }

                if (message.type === ChannelMessagesType.COLLABORATIVE_KICK_USER) {
                    alert("You've been kicked");
                    App.appView.leaveSpectatorView();
                    App.sceneManager.enableControlsObject();
                    collaborativeView.reset();
                    return;
                }

                if (message.type === ChannelMessagesType.COLLABORATIVE_KICK_NOT_INVITED) {
                    alert("You are not invited to join this room.");
                    App.appView.leaveSpectatorView();
                    App.sceneManager.enableControlsObject();
                    collaborativeView.reset();
                    return;
                }

                if (message.type === ChannelMessagesType.COLLABORATIVE_COMMANDS && collaborativeView.roomKey === message.key) {
                    if(message.messageBroadcast.cameraInfos) {
                        App.sceneManager.setControlsContext(message.messageBroadcast.cameraInfos);
                    } else if (message.messageBroadcast.smartPath){
                        App.appView.updateTreeView(message.messageBroadcast.smartPath);
                    } else if (message.messageBroadcast.editedMeshes){
                        App.sceneManager.setEditedMeshes(message.messageBroadcast.editedMeshes);
                    }
                    return;
                }

                if (message.type === ChannelMessagesType.COLLABORATIVE_GIVE_HAND && collaborativeView.roomKey === message.key) {
                    //App.appView.leaveSpectatorView();
                    //App.sceneManager.enableControlsObject();
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
