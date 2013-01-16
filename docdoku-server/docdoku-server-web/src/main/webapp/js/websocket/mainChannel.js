function sendWebRTCInviteMessage(login, context) {
    mainChannel.sendJSON({
        type: ChannelMessagesType.WEBRTC_INVITE,
        callee: login,
        context: context
    });
}

var mainChannel = new Channel("ws://localhost:8080/mainChannelSocket", "listen:" + APP_CONFIG.login);
