/*global define,App,RTCSessionDescription,RTCIceCandidate,_*/
define([
    'backbone',
    'modules/webrtc-module/adapters/webRTCAdapter',
    'text!modules/webrtc-module/templates/webrtc_module_template.html',
    'common-objects/websocket/channelMessagesType',
    'common-objects/websocket/callState',
    'common-objects/websocket/rejectCallReason'
],
function (Backbone, webRTCAdapter, template, ChannelMessagesType, CALL_STATE, REJECT_CALL_REASON) {

	'use strict';

    function extractSdp(sdpLine, pattern) {
        var result = sdpLine.match(pattern);
        return (result && result.length === 2) ? result[1] : null;
    }

    // Strip CN from sdp before CN constraints is ready.
    function removeCN(sdpLines, mLineIndex) {
        var mLineElements = sdpLines[mLineIndex].split(' ');
        // Scan from end for the convenience of removing an item.
        for (var i = sdpLines.length - 1; i >= 0; i--) {
            var payload = extractSdp(sdpLines[i], /a=rtpmap:(\d+) CN\/\d+/i);
            if (payload) {
                var cnPos = mLineElements.indexOf(payload);
                if (cnPos !== -1) {
                    // Remove CN payload from m line.
                    mLineElements.splice(cnPos, 1);
                }
                // Remove CN line in sdp
                sdpLines.splice(i, 1);
            }
        }

        sdpLines[mLineIndex] = mLineElements.join(' ');
        return sdpLines;
    }

    // Set the selected codec to the first in m line.
    function setDefaultCodec(mLine, payload) {
        var elements = mLine.split(' ');
        var newLine = [];
        var index = 0;
        for (var i = 0; i < elements.length; i++) {
            if (index === 3) { // Format of media starts from the fourth.
                newLine[index++] = payload; // Put target payload to the first.
            }
            if (elements[i] !== payload) {
                newLine[index++] = elements[i];
            }
        }
        return newLine.join(' ');
    }

    // Set Opus as the default audio codec if it's present.
    function preferOpus(sdp) {
        var sdpLines = sdp.split('\r\n');

        var mLineIndex = null;
        // Search for m line.
        for (var i = 0; i < sdpLines.length; i++) {
            if (sdpLines[i].search('m=audio') !== -1) {
                mLineIndex = i;
                break;
            }
        }
        if (mLineIndex === null) {
            return sdp;
        }

        // If Opus is available, set it as the default in m line.
        for (var j = 0; j < sdpLines.length; j++) {
            if (sdpLines[j].search('opus/48000') !== -1) {
                var opusPayload = extractSdp(sdpLines[j], /:(\d+) opus\/48000/i);
                if (opusPayload) {
                    sdpLines[mLineIndex] = setDefaultCodec(sdpLines[mLineIndex], opusPayload);
                }
                break;
            }
        }

        // Remove CN in m line and sdp.
        sdpLines = removeCN(sdpLines, mLineIndex);

        sdp = sdpLines.join('\r\n');
        return sdp;
    }




    var WebRTCModuleView = Backbone.View.extend({

        el: '#webrtc_module',

        events: {
            'click #webrtc_minimize_btn': 'onMinimizeButtonClick',
            'click #webrtc_fullscreen_btn': 'onFullScreenButtonClick',
            'click #webrtc_restore_btn': 'onRestoreButtonClick',
            'click #webrtc_hangup_btn': 'onHangupButtonClick'
        },

        // dom elements
        localVideo: null,
        remoteVideo: null,
        videoContainer: null,
        localStream: null,
        remoteStream: null,

        // peer connection
        initiator: 0,
        started: false,

        // state of current call
        callState: null,

        mediaConstraints: {
            'mandatory': {
                'OfferToReceiveAudio': true,
                'OfferToReceiveVideo': true
            },
            'optional': [{'VoiceActivityDetection': false}]
        },

        isVideoMuted: false,
        isAudioMuted: false,

        initialize: function () {
            _.bindAll(this);
            return this.el;
        },

        render: function () {

            this.$el.html(template);

            this.videoContainer = this.$('#webrtc_video_container')[0];
            this.localVideo = this.$('#webrtc_local_video')[0];
            this.remoteVideo = this.$('#webrtc_remote_video')[0];

            this.makeDraggable();
            this.delegateEvents();
            return this;

        },

        setState: function (callState) {
            this.callState = callState;
            return this;
        },

        onError: function (message) {
            this.setStatus(App.config.i18n.ERROR + ' : ' + App.config.i18n[message.error]);
        },

        onMinimizeButtonClick: function () {
            this.$el.removeClass('webrtc_shown').addClass('webrtc_minimized');
        },

        onRestoreButtonClick: function () {
            this.$el.removeClass('webrtc_minimized').addClass('webrtc_shown');
        },

        onFullScreenButtonClick: function () {
            this.videoContainer.webkitRequestFullScreen();
            this.$el.removeClass('webrtc_minimized').addClass('webrtc_shown');
        },

        onHangupButtonClick: function () {
            this.stop();
            this.exit();
        },

        onRemoteHangup: function () {
            this.setStatus(App.config.i18n.REMOTE_HANGUP);
            this.stop();
        },

        stop: function () {

            this.started = false;
            this.isAudioMuted = false;
            this.isVideoMuted = false;
            this.initiator = 0;

            if (this.pc) {
                this.pc.close();
                this.pc = null;
            }

            if (this.localStream !== null && this.localStream.stop) {
                this.localStream.stop();
                this.localStream = null;
            }

            if (this.remoteStream !== null && this.remoteStream.stop) {
                this.remoteStream.stop();
                this.remoteStream = null;
            }

            this.localVideo.style.opacity = 0;
            this.remoteVideo.style.opacity = 0;

            // say bye if state !== no call, then reset the call state to NO_CALL
            if (this.callState !== CALL_STATE.NO_CALL) {
                App.mainChannel.sendJSON({type: ChannelMessagesType.WEBRTC_BYE, roomKey: this.roomKey, remoteUser: App.config.login});
                this.setState(CALL_STATE.NO_CALL);
            }

        },

        exit: function () {
            this.$el.removeClass('webrtc_shown').removeClass('webrtc_minimized');
        },

        setRemoteUser: function (remoteUser) {
            this.remoteUser = remoteUser;
        },

        setContext: function (context) {
            this.context = context;
        },

        setRoomKey: function (roomKey) {
            this.roomKey = roomKey;
        },

        setStatus: function (status) {
            this.$('#webrtc_call_state').html(status);
        },

        setTitle: function (title) {
            this.$('#webrtc_module_title').html('<i class="fa fa-video-camera"></i> ' + App.config.i18n.CALL_TO_TITLE + ' : ' + title);
        },

        makeDraggable: function () {
            // local video is draggable
            this.$('#webrtc_local_video').draggable({
                containment: 'div#webrtc_video_container',
                position: 'absolute'
            }).css('position', 'absolute');

        },

        onNewOutgoingCall: function (sessionArgs) {

            // store rtc session vars
            this.setRemoteUser(sessionArgs.remoteUser);
            this.setContext(sessionArgs.context);
            this.setRoomKey(App.config.login + '-' + this.remoteUser);
            this.setTitle(this.remoteUser + ' | ' + this.context);

            this.$el.show();
            this.$el.removeClass('webrtc_minimized').addClass('webrtc_shown');

            if (!App.mainChannel.isReady()) {
                this.setStatus(App.config.i18n.ERROR + ' : ' + App.config.i18n.CHANNEL_NOT_READY_ERROR);
                this.stop();
                return;
            }

            if (this.callState !== CALL_STATE.NO_CALL) {
                // cannot initiate a new call in not a NO CALL state
                return;
            }

            // local user initiate the call
            this.setState(CALL_STATE.OUTGOING);
            this.setStatus(App.config.i18n.WAITING_USER_MEDIA);
            this.initMedia();
        },

        onCallAcceptedByLocalUser: function (message) {

            if (this.callState !== CALL_STATE.INCOMING) {
                return;
            }

            this.setState(CALL_STATE.NEGOTIATING);

            // remote user initiate the call
            this.initiator = 1;

            this.setRemoteUser(message.remoteUser);
            this.setContext(message.context);
            this.setRoomKey(message.roomKey);

            // tell the remote user we accept the call.
            // this will trigger a room.addUser
            App.mainChannel.sendJSON({
                type: ChannelMessagesType.WEBRTC_ACCEPT,
                roomKey: message.roomKey,
                remoteUser: this.remoteUser
            });

            this.setTitle(this.remoteUser + ' | ' + this.context);
            this.setStatus(App.config.i18n.WAITING_USER_MEDIA);
            this.$el.addClass('webrtc_shown').show();
            this.initMedia();

        },

        onCallTimeoutByLocalUser: function (message) {
            // tell the remote user we reject the call.
            App.mainChannel.sendJSON({
                type: ChannelMessagesType.WEBRTC_REJECT,
                roomKey: message.roomKey,
                remoteUser: message.remoteUser,
                reason: REJECT_CALL_REASON.TIMEOUT
            });
            this.stop();
        },

        onCallRejectedByLocalUser: function (message) {

            if (this.callState !== CALL_STATE.INCOMING) {
                return;
            }

            // tell the remote user we reject the call (reason : reject)
            App.mainChannel.sendJSON({
                type: ChannelMessagesType.WEBRTC_REJECT,
                roomKey: message.roomKey,
                remoteUser: message.remoteUser,
                reason: REJECT_CALL_REASON.REJECTED
            });

            this.stop();
        },

        onCallAcceptedByRemoteUser: function () {
            this.setState(CALL_STATE.NEGOTIATING);
            this.setStatus(App.config.i18n.REMOTE_ACCEPT);
        },

        onCallRejectedByRemoteUser: function (message) {

            switch (message.reason) {
                case REJECT_CALL_REASON.BUSY :
                    this.setStatus(App.config.i18n.REMOTE_BUSY);
                    break;
                case REJECT_CALL_REASON.TIMEOUT :
                    this.setStatus(App.config.i18n.REMOTE_TIMEOUT);
                    break;
                case REJECT_CALL_REASON.REJECTED :
                    this.setStatus(App.config.i18n.REMOTE_REJECT);
                    break;
                case REJECT_CALL_REASON.OFFLINE :
                    this.setStatus(App.config.i18n.REMOTE_OFFLINE);
                    break;
                default :
                    this.setStatus(App.config.i18n.REMOTE_REJECT);
                    break;
            }

            this.stop();
        },

        onCallHangUpByRemoteUser: function () {
            this.setStatus(App.config.i18n.REMOTE_HANGUP);
            this.stop();
        },

        initMedia: function () {
            try {
                webRTCAdapter.getUserMedia({'audio': true, 'video': {'mandatory': {}, 'optional': []}}, this.onUserMediaSuccess, this.onUserMediaError);
            } catch (e) {
                this.setStatus(App.config.i18n.USER_MEDIA_FAILED);
            }
        },

        onUserMediaSuccess: function (stream) {

            this.localStream = stream;
            webRTCAdapter.attachMediaStream(this.localVideo, this.localStream);
            this.localVideo.style.opacity = 1;

            // Caller creates PeerConnection.
            if (this.initiator) {
                this.maybeStart();
            }
            else {

                if (!this.started) {
                    // send invite to remote user
                    App.mainChannel.sendJSON({
                        type: ChannelMessagesType.WEBRTC_INVITE,
                        remoteUser: this.remoteUser,
                        context: this.context
                    });
                    this.setStatus(App.config.i18n.VIDEO_INVITATION_SENT);
                }

            }

        },

        onUserMediaError: function () {
            this.setStatus(App.config.i18n.ERROR + ' : ' + App.config.i18n.DEVICE_ERROR);
            this.stop();
        },

        maybeStart: function () {

            if (!this.started && this.localStream) {
                this.setStatus(App.config.i18n.CONNECTING);
                this.createPeerConnection();
                this.pc.addStream(this.localStream);
                this.started = true;
                if (this.initiator) {
                    this.doCall();
                }
            }

        },

        doCall: function () {
            this.pc.createOffer(this.setLocalAndSendOfferMessage, function(){
            }, this.mediaConstraints);
        },

        doAnswer: function () {
            this.pc.createAnswer(this.setLocalAndSendAnswerMessage, function(){
            }, this.mediaConstraints);
        },

        setLocalAndSendOfferMessage: function (sessionDescription) {

            sessionDescription.sdp = preferOpus(sessionDescription.sdp);
            this.pc.setLocalDescription(sessionDescription);

            App.mainChannel.sendJSON({
                type:ChannelMessagesType.WEBRTC_OFFER,
                sdp:sessionDescription.sdp,
                roomKey:this.roomKey,
                remoteUser:this.remoteUser
            });

        },
        setLocalAndSendAnswerMessage: function (sessionDescription) {

            sessionDescription.sdp = preferOpus(sessionDescription.sdp);
            this.pc.setLocalDescription(sessionDescription);

            App.mainChannel.sendJSON({
                type:ChannelMessagesType.WEBRTC_ANSWER,
                sdp:sessionDescription.sdp,
                roomKey:this.roomKey,
                remoteUser:this.remoteUser
            });

        },

        onWebRTCStatusChanged: function () {
        },

        createPeerConnection: function () {
            try {
                // Create an RTCPeerConnection via the adapter
                this.pc = new webRTCAdapter.RTCPeerConnection({'iceServers': [
                    {'url': 'stun:stun.l.google.com:19302'}
                ]});
                this.pc.onicecandidate = this.onIceCandidate.bind(this);
                this.pc.onconnecting = this.onSessionConnecting.bind(this);
                this.pc.onopen = this.onSessionOpened.bind(this);
                this.pc.onaddstream = this.onRemoteStreamAdded.bind(this);
                this.pc.onremovestream = this.onRemoteStreamRemoved.bind(this);
            } catch (e) {
                // Failed to create PeerConnection
                this.setStatus(App.config.i18n.CANNOT_CREATE_PC);
            }
        },

        processSignalingMessage: function (msg) {

            if (msg.type === ChannelMessagesType.WEBRTC_OFFER) {

                // Callee creates PeerConnection
                if (!this.initiator && !this.started) {
                    this.maybeStart();
                }

                this.pc.setRemoteDescription(new RTCSessionDescription(msg),this.onRemoteDescriptionSet.bind(this), this.onError.bind(this));

            } else if (msg.type === ChannelMessagesType.WEBRTC_ANSWER && this.started) {

                this.pc.setRemoteDescription(new RTCSessionDescription(msg), this.onRemoteDescriptionSet.bind(this), this.onError.bind(this));

            } else if (msg.type === ChannelMessagesType.WEBRTC_CANDIDATE && this.started) {

                this.pc.addIceCandidate(new RTCIceCandidate({
                    sdpMLineIndex: msg.label,
                    candidate: msg.candidate
                }));

            } else if (msg.type === ChannelMessagesType.WEBRTC_BYE) {
                this.onRemoteHangup();
            }

        },

        onRemoteDescriptionSet:function(){
            this.doAnswer();
        },

        onIceCandidate: function (event) {
            if (event.candidate) {
                App.mainChannel.sendJSON({
                    type: ChannelMessagesType.WEBRTC_CANDIDATE,
                    roomKey: this.roomKey,
                    label: event.candidate.sdpMLineIndex,
                    id: event.candidate.sdpMid,
                    candidate: event.candidate.candidate,
                    remoteUser: this.remoteUser
                });
            }
        },

        onSessionConnecting: function () {
            this.setStatus(App.config.i18n.SESSION_CONNECTING);
        },

        onSessionOpened: function () {
            this.setStatus(App.config.i18n.SESSION_OPENED);
            this.setState(CALL_STATE.RUNNING);
        },

        onRemoteStreamAdded: function (event) {
            this.setStatus(App.config.i18n.REMOTE_STEAM_ADDED);
            this.remoteStream = event.stream;
            webRTCAdapter.attachMediaStream(this.remoteVideo, this.remoteStream);
            this.waitForRemoteVideo();
        },

        onRemoteStreamRemoved: function () {
            this.setStatus(App.config.i18n.REMOTE_STEAM_REMOVED);
        },

        waitForRemoteVideo: function () {
            this.setStatus(App.config.i18n.WAITING_REMOTE_VIDEO);
            var self = this;
            try {
                //Try the new representation of tracks in a stream in M26.
                this.videoTracks = this.remoteStream.getVideoTracks();
            } catch (e) {
                this.videoTracks = this.remoteStream.videoTracks;
            }

            if (this.videoTracks.length === 0 || this.remoteVideo.readyState >= 2) {
                this.remoteVideo.style.opacity = 1;
                this.setStatus(App.config.i18n.CONNECTED);
                this.setState(CALL_STATE.RUNNING);
            } else {
                setTimeout(function () {
                    self.waitForRemoteVideo();
                }, 100);
            }
        }

    });

    return WebRTCModuleView;

});
