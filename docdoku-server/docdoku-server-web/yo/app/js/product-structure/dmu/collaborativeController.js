/*global _,define,App*/
define([
    'common-objects/websocket/channelListener',
    'common-objects/websocket/channelMessagesType'
], function (ChannelListener, ChannelMessagesType) {
	'use strict';
    function CollaborativeController() {
        var collaborativeListener;
	    var _this = this;

	    function onCreateMessage(message){
		    App.collaborativeView.roomCreated(message.key, message.remoteUser);
	    }
	    function onJoinMessage(message){
		    App.collaborativeView.setRoomKey(message.key);
		    App.appView.setConfigSpec(message.messageBroadcast.baseline);
		    App.appView.updateTreeView(message.messageBroadcast.smartPath);
		    App.sceneManager.setControlsContext(message.messageBroadcast.cameraInfos);
		    App.sceneManager.setEditedMeshes(message.messageBroadcast.editedMeshes);
		    App.sceneManager.setEditedMeshesColor(message.messageBroadcast.colourEditedMeshes);
		    App.$ControlsContainer.find('#slider-explode').val(message.messageBroadcast.explode);
		    App.sceneManager.explodeScene(message.messageBroadcast.explode);
	    }
	    function onContextMessage(message){
		    if(App.collaborativeView.roomKey === message.key){
			    if (message.messageBroadcast.master === '') {
				    App.collaborativeView.setLastMaster(message.messageBroadcast.lastMaster);
			    } else {
				    App.collaborativeView.setMaster(message.messageBroadcast.master);
			    }
			    App.collaborativeView.setUsers(message.messageBroadcast.users);
			    App.collaborativeView.setPendingUsers(message.messageBroadcast.pendingUsers);
		    }
	    }
	    function onKick(){
		    App.appView.leaveSpectatorView();
		    App.sceneManager.enableControlsObject();
		    App.collaborativeView.reset();
	    }
	    function onKickUserMessage(){
		    App.appView.crashWithMessage(App.config.i18n.COLLABORATIVE_KICKED);
		    onKick();
	    }
	    function onKickNotInvitedMessage(){
		    App.appView.crashWithMessage(App.config.i18n.COLLABORATIVE_NOT_INVITED);
		    onKick();
	    }
	    function onCollaborativeMessage(message){
		    if(App.collaborativeView.roomKey === message.key){
			    if (message.messageBroadcast.cameraInfos) {
				    App.sceneManager.setControlsContext(message.messageBroadcast.cameraInfos);
			    } else if (message.messageBroadcast.smartPath) {
				    App.appView.updateTreeView(message.messageBroadcast.smartPath);
			    } else if (message.messageBroadcast.editedMeshes) {
				    App.sceneManager.setEditedMeshes(message.messageBroadcast.editedMeshes);
			    } else if (message.messageBroadcast.baseline){
				    App.appView.setConfigSpec(message.messageBroadcast.baseline);
                    App.baselineSelectView.refresh();
                } else if (message.messageBroadcast.colourEditedMeshes !== undefined) {
				    App.sceneManager.setEditedMeshesColor(message.messageBroadcast.colourEditedMeshes);
			    } else if (message.messageBroadcast.explode) {
				    App.$ControlsContainer.find('#slider-explode').val(message.messageBroadcast.explode);
				    App.sceneManager.explodeScene(message.messageBroadcast.explode);
			    } else if (message.messageBroadcast.clipping) {
				    _this.setClippingValue(message.messageBroadcast.clipping);
			    } else if (message.messageBroadcast.layers) {
				    if (message.messageBroadcast.layers === 'remove layer') {
					    App.sceneManager.layerManager.removeAllMeshesFromMarkers();
				    }
				    App.layersListView.refreshLayers();
			    } else if (message.messageBroadcast.markers) {
				    if (message.messageBroadcast.markers === 'remove marker') {
					    App.sceneManager.layerManager.removeAllMeshesFromMarkers();
				    }
				    App.layersListView.refreshLayers();
			    }
		    }
	    }

	    collaborativeListener = new ChannelListener({

		    messagePattern: /^COLLABORATIVE_.+/,

		    isApplicable: function (messageType) {
			    return messageType.match(this.messagePattern) !== null;
		    },

		    onMessage: function (message) {
			    switch (message.type) {
				    case ChannelMessagesType.COLLABORATIVE_CREATE:
					    onCreateMessage(message);
					    break;
				    case ChannelMessagesType.COLLABORATIVE_JOIN:
					    onJoinMessage(message);
					    break;
				    case ChannelMessagesType.COLLABORATIVE_CONTEXT:
					    onContextMessage(message);
					    break;
				    case ChannelMessagesType.COLLABORATIVE_KICK_USER:
					    onKickUserMessage(message);
					    break;
				    case ChannelMessagesType.COLLABORATIVE_KICK_NOT_INVITED:
					    onKickNotInvitedMessage(message);
					    break;
				    case ChannelMessagesType.COLLABORATIVE_COMMANDS:
					    onCollaborativeMessage(message);
					    break;
				    default:
					    break;
			    }
		    },
		    onStatusChanged: function (status) {
			    // only for compliance
		    }
	    });
        App.mainChannel.addChannelListener(collaborativeListener);

        this.setClippingValue = function (value) {
            App.$ControlsContainer.find('slider-clipping').val(value);
            var max = App.SceneOptions.cameraFar * 3 / 4;
            var percentage = value * Math.log(max) / 100; // cross product to set a value to pass to the exponential function
            App.sceneManager.setCameraNear(Math.exp(percentage));
        };
        this.sendJoinRequest = function (key) {
            App.mainChannel.sendJSON({
                type: ChannelMessagesType.COLLABORATIVE_JOIN,
                key: key,
                remoteUser: ''
            });
        };
	    this.sendSmartPath = function(value) {
		    if (App.collaborativeView && App.collaborativeView.isMaster) {
			    App.mainChannel.sendJSON({
				    type: ChannelMessagesType.COLLABORATIVE_COMMANDS,
				    key: App.collaborativeView.roomKey,
				    remoteUser: '',
				    messageBroadcast: {
					    smartPath: value
				    }
			    });
		    }
	    };
	    this.sendBaseline = function(value) {
		    value = (value) ? value : App.config.configSpec;
		    if (App.collaborativeView && App.collaborativeView.isMaster) {
			    App.mainChannel.sendJSON({
				    type: ChannelMessagesType.COLLABORATIVE_COMMANDS,
				    key: App.collaborativeView.roomKey,
				    remoteUser: '',
				    messageBroadcast: {
					    baseline: value
				    }
			    });
		    }
	    };
        this.sendLayersRefresh = function (subject) {
            if (App.collaborativeView && App.collaborativeView.isMaster) {
                var message = {
                    type: ChannelMessagesType.COLLABORATIVE_COMMANDS,
                    key: App.collaborativeView.roomKey,
                    messageBroadcast: {
                        layers: subject
                    },
                    remoteUser: ''
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
                    remoteUser: ''
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
                    remoteUser: ''
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
                    remoteUser: ''
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
                    remoteUser: ''
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
                    remoteUser: ''
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
                    remoteUser: ''
                };
                App.mainChannel.sendJSON(message);
            }
        };
    }
    return CollaborativeController;
});
