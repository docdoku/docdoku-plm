/*global define,App*/
'use strict';
define([
    'common-objects/websocket/channelListener',
    "common-objects/websocket/channelMessagesType"
], function (ChannelListener, ChannelMessagesType) {

    function CollaborativeController() {

        var _this = this;

        var collaborativeListener = new ChannelListener({

            messagePattern: /^COLLABORATIVE_.+/,

            isApplicable: function (messageType) {
                return messageType.match(this.messagePattern) !== null;
            },

            onMessage: function (message) {

                if (message.type === ChannelMessagesType.COLLABORATIVE_CREATE) {
                    App.collaborativeView.roomCreated(message.key, message.remoteUser);
                    return;
                }

                if (message.type === ChannelMessagesType.COLLABORATIVE_JOIN) {
                    App.collaborativeView.setRoomKey(message.key);
                    App.appView.updateTreeView(message.messageBroadcast.smartPath);

                    App.sceneManager.setControlsContext(message.messageBroadcast.cameraInfos);
                    App.sceneManager.setEditedMeshes(message.messageBroadcast.editedMeshes);
                    App.sceneManager.setColourEditedMeshes(message.messageBroadcast.colourEditedMeshes);
                    App.$ControlsContainer.find("#slider-explode").val(message.messageBroadcast.explode);
                    App.sceneManager.explodeScene(message.messageBroadcast.explode);
                    return;
                }

                if (message.type === ChannelMessagesType.COLLABORATIVE_CONTEXT && App.collaborativeView.roomKey === message.key) {
                    if (message.messageBroadcast.master === '') {
                        App.collaborativeView.setLastMaster(message.messageBroadcast.lastMaster);
                    } else {
                        App.collaborativeView.setMaster(message.messageBroadcast.master);
                    }
                    App.collaborativeView.setUsers(message.messageBroadcast.users);
                    App.collaborativeView.setPendingUsers(message.messageBroadcast.pendingUsers);
                    return;
                }

                if (message.type === ChannelMessagesType.COLLABORATIVE_KICK_USER) {
                    alert("You've been kicked");
                    App.appView.leaveSpectatorView();
                    App.sceneManager.enableControlsObject();
                    App.collaborativeView.reset();
                    return;
                }

                if (message.type === ChannelMessagesType.COLLABORATIVE_KICK_NOT_INVITED) {
                    alert("You are not invited to join this room.");
                    App.appView.leaveSpectatorView();
                    App.sceneManager.enableControlsObject();
                    App.collaborativeView.reset();
                    return;
                }

                if (message.type === ChannelMessagesType.COLLABORATIVE_COMMANDS && App.collaborativeView.roomKey === message.key) {
                    if (message.messageBroadcast.cameraInfos) {
                        App.sceneManager.setControlsContext(message.messageBroadcast.cameraInfos);
                    } else if (message.messageBroadcast.smartPath) {
                        App.appView.updateTreeView(message.messageBroadcast.smartPath);
                    } else if (message.messageBroadcast.editedMeshes) {
                        App.sceneManager.setEditedMeshes(message.messageBroadcast.editedMeshes);
                    } else if (message.messageBroadcast.colourEditedMeshes !== undefined) {
                        App.sceneManager.setColourEditedMeshes(message.messageBroadcast.colourEditedMeshes);
                    } else if (message.messageBroadcast.explode) {
                        App.$ControlsContainer.find("#slider-explode").val(message.messageBroadcast.explode);
                        App.sceneManager.explodeScene(message.messageBroadcast.explode);
                    } else if (message.messageBroadcast.clipping) {
                        _this.setClippingValue(message.messageBroadcast.clipping);
                    } else if (message.messageBroadcast.layers) {
                        if (message.messageBroadcast.layers === "remove layer") {
                            App.sceneManager.layerManager.removeAllMeshesFromMarkers();
                        }
                        App.layersListView.refreshLayers();
                    } else if (message.messageBroadcast.markers) {
                        if (message.messageBroadcast.markers === "remove marker") {
                            App.sceneManager.layerManager.removeAllMeshesFromMarkers();
                        }
                        App.layersListView.refreshLayers();
                    }

                }

            },
            onStatusChanged: function (status) {
                // only for compliance
            }
        });

        App.mainChannel.addChannelListener(collaborativeListener);

        this.setClippingValue = function (value) {
            App.$ControlsContainer.find("slider-clipping").val(value);
            var max = App.SceneOptions.cameraFar * 3 / 4;
            var percentage = value * Math.log(max) / 100; // cross product to set a value to pass to the exponential function
            App.sceneManager.setCameraNear(Math.exp(percentage));
        };


        this.sendJoinRequest = function (key) {
            App.mainChannel.sendJSON({
                type: ChannelMessagesType.COLLABORATIVE_JOIN,
                key: key,
                remoteUser: ""
            });
        };

        this.sendLayersRefresh = function (subject) {
            if (App.collaborativeView && App.collaborativeView.isMaster) {
                var message = {
                    type: ChannelMessagesType.COLLABORATIVE_COMMANDS,
                    key: App.collaborativeView.roomKey,
                    messageBroadcast: {
                        layers: subject
                    },
                    remoteUser: ""
                };
                App.mainChannel.sendJSON(message);
            }
        };

        this.sendMarkersRefresh = function (subject) {
            if (App.collaborativeView && App.collaborativeView.isMaster) {
                var message = {
                    type: ChannelMessagesType.COLLABORATIVE_COMMANDS,
                    key: App.collaborativeView.roomKey,
                    messageBroadcast: {
                        markers: subject
                    },
                    remoteUser: ""
                };
                App.mainChannel.sendJSON(message);
            }
        };

        this.sendCameraInfos = function () {
            if (App.collaborativeView && App.collaborativeView.isMaster) {
                var message = {
                    type: ChannelMessagesType.COLLABORATIVE_COMMANDS,
                    key: App.collaborativeView.roomKey,
                    messageBroadcast: {
                        cameraInfos: App.sceneManager.getControlsContext()
                    },
                    remoteUser: ""
                };
                App.mainChannel.sendJSON(message);
            }
        };

        this.sendClippingValue = function (value) {
            if (App.collaborativeView && App.collaborativeView.isMaster) {
                var message = {
                    type: ChannelMessagesType.COLLABORATIVE_COMMANDS,
                    key: App.collaborativeView.roomKey,
                    messageBroadcast: {
                        clipping: value
                    },
                    remoteUser: ""
                };
                App.mainChannel.sendJSON(message);
            }
        };

        this.sendEditedMeshes = function () {
            if (App.collaborativeView && App.collaborativeView.isMaster) {
                var arrayIds = [];
                _.each(App.sceneManager.editedMeshes, function (val) {
                    var mesh = App.sceneManager.getMesh(val);
                    arrayIds.push({
                        uuid: val,
                        position: mesh.position,
                        scale: mesh.scale,
                        rotation: mesh.rotation
                    });
                });
                var message = {
                    type: ChannelMessagesType.COLLABORATIVE_COMMANDS,
                    key: App.collaborativeView.roomKey,
                    messageBroadcast: {
                        editedMeshes: arrayIds
                    },
                    remoteUser: ""
                };
                App.mainChannel.sendJSON(message);
            }
        };

        this.sendColourEditedMeshes = function () {
            if (App.collaborativeView && App.collaborativeView.isMaster) {
                var message = {
                    type: ChannelMessagesType.COLLABORATIVE_COMMANDS,
                    key: App.collaborativeView.roomKey,
                    messageBroadcast: {
                        colourEditedMeshes: App.sceneManager.getEditedMeshesColoured()
                    },
                    remoteUser: ""
                };
                App.mainChannel.sendJSON(message);
            }
        };


        this.sendExplodeValue = function (value) {
            if (App.collaborativeView && App.collaborativeView.isMaster) {
                var message = {
                    type: ChannelMessagesType.COLLABORATIVE_COMMANDS,
                    key: App.collaborativeView.roomKey,
                    messageBroadcast: {
                        explode: value
                    },
                    remoteUser: ""
                };
                App.mainChannel.sendJSON(message);
            }
        };


    }

    return CollaborativeController;
});
