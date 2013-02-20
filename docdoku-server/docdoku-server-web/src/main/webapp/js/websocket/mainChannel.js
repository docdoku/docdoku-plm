/*global mainChannel,ChannelListener,ChannelMessagesType*/
var mainChannel = new Channel("ws://"+window.location.host+"/mainChannelSocket", "MainChannelApplicationNewClient" + ":" + APP_CONFIG.sessionId);

Backbone.Events.on('UserStatusRequest', onUserStatusRequest);

function onUserStatusRequest(remoteUser){
    mainChannel.sendJSON({
        type:ChannelMessagesType.USER_STATUS,
        remoteUser:remoteUser
    });
}

var userStatusListener = new ChannelListener({

    isApplicable:function(messageType){
        return messageType == ChannelMessagesType.USER_STATUS ;
    },

    onMessage:function(message){
        Backbone.Events.trigger('UserStatusRequestDone', message);
    },

    onStatusChanged:function(status){
        // nothing to do ...
    }

});

mainChannel.addChannelListener(userStatusListener);
