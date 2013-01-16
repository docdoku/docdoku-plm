define([
    "modules/webrtc-module/i18n",
    "modules/webrtc-module/views/webrtc_module_view"
], function (i18n, WebRTCModuleView) {

    var webRTCModuleView = new WebRTCModuleView();

    // handle tests
    // trigger it : Backbone.Events.trigger('WebRTCTest');
    Backbone.Events.on('WebRTCTest', webRTCModuleView.runCall);

    // handle accept
    Backbone.Events.on('AcceptWebRTCInvite', webRTCModuleView.onAcceptWebRTCInvite);

    // handle reject
    Backbone.Events.on('RejectWebRTCInvite', webRTCModuleView.onRejectWebRTCInvite);

    // handle status change
    Backbone.Events.on('WebRTCStatusChanged', webRTCModuleView.onWebRTCStatusChanged);

    // handle new session
    Backbone.Events.on('NewWebRTCSession', webRTCModuleView.onNewWebRTCSession);

    // Websocket Listener
    var webRTCInvitationListener = new ChannelListener({

        isApplicable: function (messageType) {
            return messageType == ChannelMessagesType.WEBRTC_INVITE;
        },

        onMessage: function (message) {

            var notification = {

                title: i18n["VIDEO_INVITE_NOTIFICATION_TITLE"],

                content: message.remoteUser + " " + i18n["VIDEO_INVITE"],

                actions: [
                    {
                        title: i18n["ACCEPT_VIDEO_INVITE"],

                        handler: function () {
                            Backbone.Events.trigger('AcceptWebRTCInvite', message);
                        }
                    },
                    {
                        title: i18n["REJECT_VIDEO_INVITE"],

                        handler: function () {
                            Backbone.Events.trigger('RejectWebRTCInvite', message);
                        }
                    }
                ]

            };

            Backbone.Events.trigger('NewNotification', notification);

        },

        onStatusChanged: function (status) {
            Backbone.Events.trigger('WebRTCStatusChanged', status);
        }

    });

    mainChannel.addChannelListener(webRTCInvitationListener);

});
/*

function videoCall(authorLogin) {

    var webRtcModal = $("#webRtcModal");
    var webRtcModalBody = webRtcModal.find(".modal-body");
    var webRtcModalTitle = webRtcModal.find("h3");


    webRtcModal.one('shown', function () {
        webRtcModalBody.html("<iframe src=\"" + getWebRtcUrlRoom(authorLogin) + "\" />");
        webRtcModalTitle.text("Call to " + authorLogin);
    });
    webRtcModal.one('hidden', function () {
        webRtcModalBody.empty();
    });

    webRtcModal.modal('show');
}

function getWebRtcUrlRoom(authorLogin) {
    var getIntFromString = function (str) {
        var count = 0;
        for (var i = 0; i < str.length; i++) {
            count += str.charCodeAt(i) * 60 * i;
        }
        return count;
    }
    return "/webRTCRoom?r=" + getIntFromString(authorLogin);
}
*/