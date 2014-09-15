/*global define*/
define(
    [
        'backbone',
        "modules/coworkers-access-module/views/coworkers_access_module_view",
        "common-objects/websocket/channelMessagesType"
    ],
    function (Backbone, CoWorkersAccessModuleView, ChannelMessagesType) {

        function onUserStatusRequest(remoteUser) {
            App.mainChannel.sendJSON({
                type: ChannelMessagesType.USER_STATUS,
                remoteUser: remoteUser
            });
        }

        Backbone.Events.on('UserStatusRequest', onUserStatusRequest);

        return CoWorkersAccessModuleView;
    });