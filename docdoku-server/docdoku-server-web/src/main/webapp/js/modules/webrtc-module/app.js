define([
    "i18n!localization/nls/webrtc-module-strings",
    "modules/webrtc-module/views/webrtc_module_view"
], function (i18n, WebRTCModuleView) {

    var WEBRTC_CONFIG = {
        ms_timeout : 30000
    };

    var webRTCModuleView = new WebRTCModuleView().render();

    // handle new webRtc session initiated by local user
    Backbone.Events.on('NewWebRTCSession', webRTCModuleView.onNewWebRTCSession);

    // handle reject for local user
    Backbone.Events.on('CallRejectedByLocalUser', webRTCModuleView.onCallRejectedByLocalUser);

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
                webRTCModuleView.onCallAcceptedByRemoteUser(message);
                return;
            }

            if(message.type == ChannelMessagesType.WEBRTC_REJECT){
                // remote user reject the invite
                webRTCModuleView.onCallRejectedByRemoteUser(message);
                return;
            }

            if(message.type == ChannelMessagesType.WEBRTC_HANGUP){
                // remote user hang up the call
                webRTCModuleView.onCallHangUpByRemoteUser(message);
                return;
            }

            if(message.type == ChannelMessagesType.WEBRTC_INVITE_TIMEOUT){
                // remote user timed out
                webRTCModuleView.onRemoteTimeout(message);
                return;
            }

            if(message.type == ChannelMessagesType.WEBRTC_INVITE){

                // remote user invites local user

                var notificationId = "notification_" + new Date().getTime();

                // Set a timeout (eg : local user is away from keyboard)
                var timeout = setTimeout(function(){

                    webRTCModuleView.onLocalTimeout(message);
                    Backbone.Events.trigger('RemoveNotificationRequest', notificationId);

                }, WEBRTC_CONFIG.ms_timeout);

                var notification = {

                    id : notificationId,

                    title: i18n.VIDEO_INVITE_NOTIFICATION_TITLE,

                    content: message.remoteUser + " " + i18n.VIDEO_INVITE_TEXT,

                    actions: [
                        {
                            title: i18n.ACCEPT_VIDEO_INVITE,

                            handler: function () {
                                clearTimeout(timeout);
                                webRTCModuleView.onCallAcceptedByLocalUser(message);
                                Backbone.Events.trigger('RemoveNotificationRequest', notificationId);

                            }
                        },
                        {
                            title: i18n.REJECT_VIDEO_INVITE,

                            handler: function () {
                                clearTimeout(timeout);
                                webRTCModuleView.onCallRejectedByLocalUser(message);
                                Backbone.Events.trigger('RemoveNotificationRequest', notificationId);
                            }
                        }
                    ]

                };

                // trigger notification display in nav bar
                Backbone.Events.trigger('NewNotification', notification);
            }

        },

        onStatusChanged: function (status) {
            webRTCModuleView.onWebRTCStatusChanged(status);
        }

    });

    mainChannel.addChannelListener(webRTCInvitationListener);

});