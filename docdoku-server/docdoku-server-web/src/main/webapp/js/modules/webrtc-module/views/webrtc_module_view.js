define(
    [
        "i18n!localization/nls/webrtc-module-strings",
        "modules/webrtc-module/adapters/attachMediaStream",
        "modules/webrtc-module/adapters/peerConnection",
        "modules/webrtc-module/adapters/userMedia",
        "text!modules/webrtc-module/templates/webrtc_module_template.html"
    ],
    function (i18n,attachMediaStream, RTCPeerConnection, getUserMedia, template) {

        WebRTCModuleView = Backbone.View.extend({

            el: "#webrtc_module",

            events: {
                "click #webrtc_minimize_btn": "onMinimizeButtonClick",
                "click #webrtc_fullscreen_btn": "onFullScreenButtonClick",
                "click #webrtc_restore_btn": "onRestoreButtonClick",
                "click #webrtc_hangup_btn": "onHangupButtonClick"
            },

            // dom elements
            localVideo: null,
            remoteVideo: null,
            videoContainer: null,
            localStream: null,
            remoteStream: null,

            // peer connection
            pc: null,
            initiator: 0,
            started: false,

            mediaConstraints: {
                'mandatory': {
                    'OfferToReceiveAudio': true,
                    'OfferToReceiveVideo': true
                }
            },

            isVideoMuted: false,
            isAudioMuted: false,

            initialize: function () {
                _.bindAll(this);
                return this.el;
            },

            render: function () {

                $(this.el).html(template);

                this.videoContainer = this.$("#webrtc_video_container")[0];
                this.localVideo = this.$("#webrtc_local_video")[0];
                this.remoteVideo = this.$("#webrtc_remote_video")[0];

                this.makeDraggable();
                this.delegateEvents();
                return this;

            },

            onError: function (message) {
                this.setStatus(i18n.ERROR+" : "+i18n[message.error]);
            },

            onMinimizeButtonClick: function (ev) {
                //console.log("min")
                this.$el.removeClass("webrtc_shown").addClass("webrtc_minimized");
            },

            onRestoreButtonClick: function () {
                //console.log("restore")
                this.$el.removeClass("webrtc_minimized").addClass("webrtc_shown");
            },

            onFullScreenButtonClick: function (ev) {
                this.videoContainer.webkitRequestFullScreen();
            },

            onHangupButtonClick: function (ev) {

                if (this.started) {
                    mainChannel.sendJSON({type: ChannelMessagesType.WEBRTC_BYE, roomKey: this.roomKey, remoteUser : APP_CONFIG.login});
                }

                this.stop();
                this.exit();
            },

            onRemoteHangup: function () {
                this.setStatus(i18n.REMOTE_HANGUP);
                this.stop();
            },

            stop: function () {

                this.started = false;
                this.isAudioMuted = false;
                this.isVideoMuted = false;
                this.initiator = 0;

                if (this.pc) {
                    console.log(i18n.CLOSING_PEER_CON);
                    this.pc.close();
                    this.pc = null;
                }

                if(this.localStream != null && this.localStream.stop){
                    this.localStream.stop();
                    this.localStream = null;
                }

                if(this.remoteStream != null && this.remoteStream.stop){
                    this.remoteStream.stop();
                    this.remoteStream = null;
                }

                this.localVideo.style.opacity = 0;
                this.remoteVideo.style.opacity = 0;

            },

            exit: function () {
                this.$el.removeClass("webrtc_shown").removeClass("webrtc_minimized");
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
                this.$("#webrtc_call_state").html(status);
            },

            setTitle: function (title) {
                this.$("#webrtc_module_title").html("<i class='icon-facetime-video'></i> " +i18n.CALL_TO_TITLE + " : " + title);
            },

            makeDraggable: function () {
                // local video is draggable
                this.$("#webrtc_local_video").draggable({
                    containment: 'div#webrtc_video_container',
                    position: "absolute"
                }).css("position", "absolute");

            },

            onNewWebRTCSession: function (sessionArgs) {

                // local user initiate the call

                // store rtc session vars
                this.setRemoteUser(sessionArgs.remoteUser);
                this.setContext(sessionArgs.context);
                this.setRoomKey(APP_CONFIG.login + "-" + this.remoteUser);

                this.setTitle(this.remoteUser + " | " + this.context);
                this.setStatus(i18n.WAITING_USER_MEDIA);
                this.$el.show();
                this.$el.removeClass("webrtc_minimized").addClass("webrtc_shown");

                this.initMedia();
            },


            onCallAcceptedByLocalUser: function (message) {

                // remote user initiate the call
                this.initiator = 1;

                this.setRemoteUser(message.remoteUser);
                this.setContext(message.context);
                this.setRoomKey(message.roomKey);

                // tell the remote user we accept the call.
                // this will trigger a room.addUser
                mainChannel.sendJSON({
                    type: ChannelMessagesType.WEBRTC_ACCEPT,
                    roomKey: message.roomKey,
                    remoteUser: this.remoteUser
                });

                this.setTitle(this.remoteUser + " | " + this.context);
                this.setStatus(i18n.WAITING_USER_MEDIA);
                this.$el.show();
                this.$el.addClass("webrtc_shown");

                this.initMedia();

            },

            onCallRejectedByLocalUser: function (message) {
                //console.log("Local user has rejected.");
                // tell the remote user we reject the call.
                mainChannel.sendJSON({
                    type: ChannelMessagesType.WEBRTC_REJECT,
                    roomKey: message.roomKey,
                    remoteUser: message.remoteUser
                });

            },

            onLocalTimeout: function (message) {
                // tell the remote user we didn't answer in time
                mainChannel.sendJSON({
                    type: ChannelMessagesType.WEBRTC_INVITE_TIMEOUT,
                    roomKey: message.roomKey,
                    remoteUser: message.remoteUser
                });
            },

            onRemoteTimeout: function (message) {
                this.setStatus(i18n.REMOTE_TIMEOUT);
                this.stop();
            },

            onCallAcceptedByRemoteUser: function (message) {
                this.setStatus(i18n.REMOTE_ACCEPT);
            },

            onCallRejectedByRemoteUser: function () {
                this.setStatus(i18n.REMOTE_REJECT);
                this.stop();
            },

            onCallHangUpByRemoteUser: function (message) {
                this.setStatus(i18n.REMOTE_HANGUP);
                this.stop();
            },

            initMedia: function () {

                var constraints = {"mandatory": {}, "optional": []};

                try {
                    getUserMedia({'audio': true, 'video': constraints}, this.onUserMediaSuccess, this.onUserMediaError);
                } catch (e) {
                    this.setStatus(i18n.USER_MEDIA_FAILED);
                }

            },

            onUserMediaSuccess: function (stream) {

                this.localStream = stream;
                attachMediaStream(this.localVideo, this.localStream);
                this.localVideo.style.opacity = 1;

                // Caller creates PeerConnection.
                if (this.initiator)
                    this.maybeStart();
                else {

                    if (!this.started) {
                        // send invite to remote user
                        mainChannel.sendJSON({
                            type: ChannelMessagesType.WEBRTC_INVITE,
                            remoteUser: this.remoteUser,
                            context: this.context
                        });
                        this.setStatus(i18n.VIDEO_INVITATION_SENT);
                    }

                }

            },

            onUserMediaError: function (error) {
                mainChannel.sendJSON({type: ChannelMessagesType.WEBRTC_BYE, roomKey: this.roomKey});
                this.stop();
                this.exit();
            },


            maybeStart: function () {

                if (!this.started && this.localStream) {
                    this.setStatus(i18n.CONNECTING);
                    this.createPeerConnection();
                    this.pc.addStream(this.localStream);
                    this.started = true;
                    if (this.initiator)
                        this.doCall();
                }

            },

            doCall: function () {
                this.pc.createOffer(this.setLocalAndSendMessage, null, this.mediaConstraints);
            },

            doAnswer: function () {
                this.pc.createAnswer(this.setLocalAndSendMessage, null, this.mediaConstraints);
            },

            setLocalAndSendMessage: function (sessionDescription) {
                sessionDescription.sdp = preferOpus(sessionDescription.sdp);
                this.pc.setLocalDescription(sessionDescription);
                sessionDescription.roomKey = this.roomKey;
                mainChannel.sendJSON(sessionDescription);
            },

            onWebRTCStatusChanged: function (status) {
                //console.log("onWebRTCStatusChanged." + status);
            },

            createPeerConnection: function () {

                var pc_config = {"iceServers": [
                    {"url": "stun:stun.l.google.com:19302"}
                ]};

                try {
                    // Create an RTCPeerConnection via the adapter
                    this.pc = new RTCPeerConnection(pc_config);
                    this.pc.onicecandidate = this.onIceCandidate;
                    //console.log("Created RTCPeerConnnection with config:\n" + "  \"" +JSON.stringify(pc_config) + "\".");
                } catch (e) {
                    // Failed to create PeerConnection
                    this.setStatus(i18n.CANNOT_CREATE_PC);
                    return;
                }

                this.pc.onconnecting = this.onSessionConnecting;
                this.pc.onopen = this.onSessionOpened;
                this.pc.onaddstream = this.onRemoteStreamAdded;
                this.pc.onremovestream = this.onRemoteStreamRemoved;
            },

            processSignalingMessage: function (msg) {
                //console.log("processSignalingMessage")
                if (msg.type === ChannelMessagesType.WEBRTC_OFFER) {

                    // Callee creates PeerConnection
                    if (!this.initiator && !this.started)
                        this.maybeStart();

                    this.pc.setRemoteDescription(new RTCSessionDescription(msg));
                    this.doAnswer();

                } else if (msg.type === ChannelMessagesType.WEBRTC_ANSWER && this.started) {

                    this.pc.setRemoteDescription(new RTCSessionDescription(msg));

                } else if (msg.type === ChannelMessagesType.WEBRTC_CANDIDATE && this.started) {

                    this.pc.addIceCandidate(new RTCIceCandidate({
                        sdpMLineIndex: msg.label,
                        candidate: msg.candidate
                    }));

                } else if (msg.type === ChannelMessagesType.WEBRTC_BYE) {

                    this.onRemoteHangup();

                }

            },

            onIceCandidate: function (event) {
                if (event.candidate) {
                    mainChannel.sendJSON({
                        type: ChannelMessagesType.WEBRTC_CANDIDATE,
                        roomKey: this.roomKey,
                        label: event.candidate.sdpMLineIndex,
                        id: event.candidate.sdpMid,
                        candidate: event.candidate.candidate
                    });
                }
            },

            onSessionConnecting: function (message) {
                this.setStatus(i18n.SESSION_CONNECTING);
            },

            onSessionOpened: function (message) {
                this.setStatus(i18n.SESSION_OPENED);
            },

            onRemoteStreamAdded: function (event) {
                this.setStatus(i18n.REMOTE_STEAM_ADDED);
                this.remoteStream = event.stream;
                attachMediaStream(this.remoteVideo, this.remoteStream);
                this.waitForRemoteVideo();
            },

            onRemoteStreamRemoved: function (event) {
                this.setStatus(i18n.REMOTE_STEAM_REMOVED);
            },

            waitForRemoteVideo: function () {
                this.setStatus(i18n.WAITING_REMOTE_VIDEO);
                var self = this;
                try {
                    // Try the new representation of tracks in a stream in M26.
                    this.videoTracks = this.remoteStream.getVideoTracks()
                } catch (e) {
                    this.videoTracks = this.remoteStream.videoTracks
                }

                if (this.videoTracks.length === 0 || this.remoteVideo.currentTime > 0) {
                    this.remoteVideo.style.opacity = 1;
                    this.setStatus(i18n.CONNECTED);
                } else {
                    setTimeout(function () {
                        self.waitForRemoteVideo();
                    }, 100);
                }
            }

        });


        // Set Opus as the default audio codec if it's present.
        function preferOpus(sdp) {
            var sdpLines = sdp.split('\r\n');

            // Search for m line.
            for (var i = 0; i < sdpLines.length; i++) {
                if (sdpLines[i].search('m=audio') !== -1) {
                    var mLineIndex = i;
                    break;
                }
            }
            if (mLineIndex === null)
                return sdp;

            // If Opus is available, set it as the default in m line.
            for (var i = 0; i < sdpLines.length; i++) {
                if (sdpLines[i].search('opus/48000') !== -1) {
                    var opusPayload = extractSdp(sdpLines[i], /:(\d+) opus\/48000/i);
                    if (opusPayload)
                        sdpLines[mLineIndex] = setDefaultCodec(sdpLines[mLineIndex], opusPayload);
                    break;
                }
            }

            // Remove CN in m line and sdp.
            sdpLines = removeCN(sdpLines, mLineIndex);

            sdp = sdpLines.join('\r\n');
            return sdp;
        }

        function extractSdp(sdpLine, pattern) {
            var result = sdpLine.match(pattern);
            return (result && result.length == 2) ? result[1] : null;
        }

        // Set the selected codec to the first in m line.
        function setDefaultCodec(mLine, payload) {
            var elements = mLine.split(' ');
            var newLine = new Array();
            var index = 0;
            for (var i = 0; i < elements.length; i++) {
                if (index === 3) // Format of media starts from the fourth.
                    newLine[index++] = payload; // Put target payload to the first.
                if (elements[i] !== payload)
                    newLine[index++] = elements[i];
            }
            return newLine.join(' ');
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

        return WebRTCModuleView;

    });