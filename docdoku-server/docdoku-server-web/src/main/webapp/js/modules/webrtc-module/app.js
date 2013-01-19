define([
    "modules/webrtc-module/i18n",
    "modules/webrtc-module/views/webrtc_module_view"
], function (i18n, WebRTCModuleView) {

    var webRTCModuleView = new WebRTCModuleView().render();

    // handle new webRtc session initiated by local user
    Backbone.Events.on('NewWebRTCSession', webRTCModuleView.onNewWebRTCSession);

    // handle accept for local user
    Backbone.Events.on('CallAcceptedByLocalUser', webRTCModuleView.onCallAcceptedByLocalUser);
    // handle reject for local user
    Backbone.Events.on('CallRejectedByLocalUser', webRTCModuleView.onCallRejectedByLocalUser);

    // handle accept for remote user
    Backbone.Events.on('CallAcceptedByRemoteUser', webRTCModuleView.onCallAcceptedByRemoteUser);
    // handle reject for remote user
    Backbone.Events.on('CallRejectedByRemoteUser', webRTCModuleView.onCallRejectedByRemoteUser);

    // handle hang up from remote
    Backbone.Events.on('CallHangUpByRemoteUser', webRTCModuleView.onCallHangUpByRemoteUser);

    // handle status change for channel listener
    Backbone.Events.on('WebRTCStatusChanged', webRTCModuleView.onWebRTCStatusChanged);

    // Websocket Listener
    var webRTCInvitationListener = new ChannelListener({

        // messages listened
        messagePattern : /^WEBRTC_.+/,

        webRtcSignalTypes : [
            ChannelMessagesType.WEBRTC_ANSWER,
            ChannelMessagesType.WEBRTC_BYE,
            ChannelMessagesType.WEBRTC_CANDIDATE,
            ChannelMessagesType.WEBRTC_OFFER
        ],

        isApplicable: function (messageType) {
            return messageType.match(this.messagePattern) != null
                || _.indexOf(this.webRtcSignalTypes,messageType) > -1 ;
        },

        onMessage: function (message) {

            if(message.error){
                webRTCModuleView.onError(message);
                return;
            }

            if(_.indexOf(this.webRtcSignalTypes,message.type) > -1){
                webRTCModuleView.processSignalingMessage(message);
                return;
            }

            if(message.type == ChannelMessagesType.WEBRTC_ACCEPT){
                // remote user accept
                Backbone.Events.trigger('CallAcceptedByRemoteUser', message);
                return;
            }

            if(message.type == ChannelMessagesType.WEBRTC_REJECT){
                // remote user reject the invite
                Backbone.Events.trigger('CallRejectedByRemoteUser', message);
                return;
            }

            if(message.type == ChannelMessagesType.WEBRTC_HANGUP){
                // remote user reject
                Backbone.Events.trigger('CallHangUpByRemoteUser', message);
                return;
            }

            if(message.type == ChannelMessagesType.WEBRTC_INVITE){

                // remote user invites local user

                var notification = {

                    title: i18n["VIDEO_INVITE_NOTIFICATION_TITLE"],

                    content: message.remoteUser + " " + i18n["VIDEO_INVITE"],

                    actions: [
                        {
                            title: i18n["ACCEPT_VIDEO_INVITE"],

                            handler: function () {
                                Backbone.Events.trigger('CallAcceptedByLocalUser', message);
                            }
                        },
                        {
                            title: i18n["REJECT_VIDEO_INVITE"],

                            handler: function () {
                                Backbone.Events.trigger('CallRejectedByLocalUser', message);
                            }
                        }
                    ]

                };

                Backbone.Events.trigger('NewNotification', notification);

            }

        },

        onStatusChanged: function (status) {
            Backbone.Events.trigger('WebRTCStatusChanged', status);
        }

    });

    mainChannel.addChannelListener(webRTCInvitationListener);

});