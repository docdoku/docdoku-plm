/*global Channel,ChannelListener,ChannelMessagesType*/
var mainChannel = new Channel("ws://"+window.location.host+"/mainChannelSocket", ChannelMessagesType.PEER_DECLARATION);

function onUserStatusRequest(remoteUser){
    mainChannel.sendJSON({
        type:ChannelMessagesType.USER_STATUS,
        remoteUser:remoteUser
    });
}

Backbone.Events.on('UserStatusRequest', onUserStatusRequest);

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
