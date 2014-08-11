/*global App,ChannelListener,ChannelMessagesType,mainChannel*/
'use strict';
define(function(){

    function CollaborativeController () {
        var _this = this;
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
                        collaborativeView.setLastMaster(message.messageBroadcast.lastMaster);
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
                    }else if (message.messageBroadcast.colourEditedMeshes !== undefined){
                        App.sceneManager.setColourEditedMeshes(message.messageBroadcast.colourEditedMeshes);
                    }else if (message.messageBroadcast.explode){
                        document.getElementById("slider-explode").value = message.messageBroadcast.explode;
                        App.sceneManager.explodeScene(message.messageBroadcast.explode);
                    }else if (message.messageBroadcast.layers){
                        if(message.messageBroadcast.layers === "remove layer"){
                            App.sceneManager.layerManager.removeAllMeshesFromMarkers();
                        }
                        App.layersListView.refreshLayers();
                    }else if (message.messageBroadcast.markers){
                        if(message.messageBroadcast.markers === "remove marker"){
                            App.sceneManager.layerManager.removeAllMeshesFromMarkers();
                        }
                        App.layersListView.refreshLayers();
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

        this.sendLayersRefresh = function(subject){
            _this = this;
            if (App.collaborativeView.isMaster) {
                var message = {
                    type: ChannelMessagesType.COLLABORATIVE_COMMANDS,
                    key: App.collaborativeView.roomKey,
                    messageBroadcast: {
                        layers: subject
                    },
                    remoteUser: ""
                };
                mainChannel.sendJSON(message);
            }
        };

        this.sendMarkersRefresh = function(subject){
            _this = this;
            if (App.collaborativeView.isMaster) {
                var message = {
                    type: ChannelMessagesType.COLLABORATIVE_COMMANDS,
                    key: App.collaborativeView.roomKey,
                    messageBroadcast: {
                        markers: subject
                    },
                    remoteUser: ""
                };
                mainChannel.sendJSON(message);
            }
        };
    }

    return CollaborativeController;
});
