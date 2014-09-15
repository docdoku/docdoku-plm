/*global define*/
define([
    'backbone',
    'common-objects/websocket/channel',
    'common-objects/websocket/channelListener',
    'common-objects/websocket/channelMessagesType',
    'modules/coworkers-access-module/app',
    'modules/user-popover-module/app',
    'modules/chat-module/app',
    'modules/webrtc-module/app'
], function (Backbone, Channel, ChannelListener, ChannelMessagesType, CoWorkersAccessModuleView, UserPopoverModule, chatListener, webRTCInvitationListener) {

    var page_unload = function () {
        App.mainChannel.ws.onclose = function () {
        };
        App.mainChannel.ws.close();
    };

    var userStatusListener = new ChannelListener({

        isApplicable: function (messageType) {
            return messageType === ChannelMessagesType.USER_STATUS;
        },

        onMessage: function (message) {
            Backbone.Events.trigger('UserStatusRequestDone', message);
        },

        onStatusChanged: function (status) {

        }

    });

    window.addEventListener('beforeunload', page_unload, false);

    App.mainChannel = new Channel();
    App.mainChannel.addChannelListener(userStatusListener);
    App.mainChannel.addChannelListener(webRTCInvitationListener);
    App.mainChannel.addChannelListener(chatListener);
    App.mainChannel.init('ws://' + window.location.host + APP_CONFIG.contextPath + '/mainChannelSocket');

    return {
        CoWorkersAccessModuleView: CoWorkersAccessModuleView
    };

});