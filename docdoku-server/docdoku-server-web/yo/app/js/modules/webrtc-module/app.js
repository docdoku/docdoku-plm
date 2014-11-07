/*global _,define,App*/
define([
    'backbone',
    'buzz',
    'modules/webrtc-module/views/webrtc_module_view',
    'common-objects/websocket/channelListener',
    'common-objects/websocket/channelMessagesType',
    'common-objects/websocket/callState',
    'common-objects/websocket/rejectCallReason'
], function (Backbone, buzz, WebRTCModuleView, ChannelListener, ChannelMessagesType, CALL_STATE, REJECT_CALL_REASON) {
	'use strict';
    var WEBRTC_CONFIG = {
        MS_TIMEOUT: 30000,
        PLAY_SOUND: true
    };

    Backbone.Events.on('NotificationSound', function () {
        new buzz.sound(App.config.contextPath + '/sounds/notification.ogg').play();
    });

    Backbone.Events.on('IncomingCallSound', function () {
        if (WEBRTC_CONFIG.PLAY_SOUND) {
            new buzz.sound(App.config.contextPath + '/sounds/incoming-call.ogg').play();
        }
    });


    // init the web rtc call manager
    var webRTCModuleView = new WebRTCModuleView().setState(CALL_STATE.NO_CALL).render();

    // Listen to call events

    // handle new webRtc session initiated by local user
    // the room key is known : 'localUserLogin-remoteUserLogin'
    Backbone.Events.on('NewOutgoingCall', webRTCModuleView.onNewOutgoingCall);

    // handle reject for local user
    Backbone.Events.on('CallRejectedByLocalUser', webRTCModuleView.onCallRejectedByLocalUser);

    // Websocket Listener
    var webRTCInvitationListener = new ChannelListener({

        // messages listened
        messagePattern: /^WEBRTC_.+/,

        webRtcSignalTypes: [
            ChannelMessagesType.WEBRTC_ANSWER,
            ChannelMessagesType.WEBRTC_BYE,
            ChannelMessagesType.WEBRTC_CANDIDATE,
            ChannelMessagesType.WEBRTC_OFFER
        ],

        isApplicable: function (messageType) {
            return messageType.match(this.messagePattern) !== null ||
	            _.indexOf(this.webRtcSignalTypes, messageType) > -1;
        },

        onMessage: function (message) {

            // error messages
            if (message.error && webRTCModuleView.roomKey === message.roomKey) {
                webRTCModuleView.onError(message);
                return;
            }

            // forwarded messages from the remote peer (web rtc view must be in an active call state)
            // ensure the call state is either negotiating or running, and the room key is the right one
            if (_.contains(this.webRtcSignalTypes, message.type) &&
	            (webRTCModuleView.callState === CALL_STATE.NEGOTIATING || webRTCModuleView.callState === CALL_STATE.RUNNING) &&
	            webRTCModuleView.roomKey === message.roomKey) {

                webRTCModuleView.processSignalingMessage(message);
                return;
            }

            // remote user has accepted the call, both users should be in the room
            if (message.type === ChannelMessagesType.WEBRTC_ACCEPT && webRTCModuleView.roomKey === message.roomKey && webRTCModuleView.callState === CALL_STATE.OUTGOING) {
                webRTCModuleView.onCallAcceptedByRemoteUser(message);
                return;
            }

            // remote user has rejected the call (the message contains the reason)
            if (message.type === ChannelMessagesType.WEBRTC_REJECT && webRTCModuleView.roomKey === message.roomKey && webRTCModuleView.callState === CALL_STATE.OUTGOING) {
                webRTCModuleView.onCallRejectedByRemoteUser(message);
                return;
            }

            // remote user hang up the call (local user must be in NEGOTIATING or RUNNING state)
            if (message.type === ChannelMessagesType.WEBRTC_HANGUP && webRTCModuleView.roomKey === message.roomKey && (webRTCModuleView.callState === CALL_STATE.RUNNING || webRTCModuleView.callState === CALL_STATE.NEGOTIATING)) {
                webRTCModuleView.onCallHangUpByRemoteUser(message);
                return;
            }

            // Receiving a WebRTC invitation
            // NO CALL state : prompt the local user to accept / reject
            // Other states : local user is busy.

            if (message.type === ChannelMessagesType.WEBRTC_INVITE) {


                // TODO : remove this :
                // webRTCModuleView.callState = CALL_STATE.RUNNING;
                // TODO : -----


                // If local user is busy ...
                if (webRTCModuleView.callState !== CALL_STATE.NO_CALL) {

                    // tell the remote that the local user reject the call because he's busy
                    App.mainChannel.sendJSON({
                        type: ChannelMessagesType.WEBRTC_REJECT,
                        roomKey: message.roomKey,
                        remoteUser: message.remoteUser,
                        reason: REJECT_CALL_REASON.BUSY
                    });

                    // TODO : keep track of this invite and notify local user of the call

                    return;
                }

                // Local user is in a NO_CALL state, let's prompt him.
                else {
                    Backbone.Events.trigger('IncomingCallSound');
                    // Set the call state to incoming, hook data on view
                    webRTCModuleView.setState(CALL_STATE.INCOMING);
                    webRTCModuleView.setRoomKey(message.roomKey);
                    webRTCModuleView.setRemoteUser(message.remoteUser);
                    webRTCModuleView.setContext(message.context);

                    var webRTCInvitation = {

                        message: message,

                        accept: function () {
                            clearTimeout(this.invitationTimeout);
                            webRTCModuleView.onCallAcceptedByLocalUser(message);
                        },

                        reject: function () {
                            clearTimeout(this.invitationTimeout);
                            webRTCModuleView.onCallRejectedByLocalUser(message);
                        }

                    };

                    webRTCInvitation.invitationTimeout = setTimeout(function () {

                        // Timeout reached, remove the invitation
                        Backbone.Events.trigger('RemoveWebRTCInvitation', webRTCInvitation);

                        // Send a reject (reason : timeout) if call remains incoming
                        if (webRTCModuleView.callState === CALL_STATE.INCOMING) {
                            webRTCModuleView.onCallTimeoutByLocalUser(message);
                        }
                        // stop local session
                        else {
                            webRTCModuleView.setState(CALL_STATE.NO_CALL);
                            webRTCModuleView.stop();
                        }

                    }, WEBRTC_CONFIG.MS_TIMEOUT);

                    // trigger notification display in a chat session
                    Backbone.Events.trigger('NewWebRTCInvitation', webRTCInvitation);
                }

            }

            // remove invitation if local user has accepted or rejected the invitation from an other socket (an other tab)

            if ((message.type === ChannelMessagesType.WEBRTC_ROOM_JOIN_EVENT || message.type === ChannelMessagesType.WEBRTC_ROOM_REJECT_EVENT) &&
	            message.userLogin === App.config.login &&
	            webRTCModuleView.callState === CALL_STATE.INCOMING && message.roomKey === webRTCModuleView.roomKey) {

                if (webRTCModuleView.remoteUser) {

                    // hooked data needed
                    message.remoteUser = webRTCModuleView.remoteUser;
                    message.context = webRTCModuleView.context;

                    Backbone.Events.trigger('RemoveWebRTCInvitation', {message: message});

                    if (message.type === ChannelMessagesType.WEBRTC_ROOM_REJECT_EVENT) {
                        webRTCModuleView.onCallRejectedByLocalUser(message);
                    } else {
                        webRTCModuleView.setState(CALL_STATE.NO_CALL);
                        webRTCModuleView.stop();
                    }

                }
            }

        },

        onStatusChanged: function (status) {
            webRTCModuleView.onWebRTCStatusChanged(status);
        }

    });

    return webRTCInvitationListener;


});