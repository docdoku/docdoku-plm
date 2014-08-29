var ChannelStatus = {
    OPENED : 'opened',
    CLOSED : 'closed'
};

var ChannelMessagesType = {

    WEBRTC_INVITE: 'WEBRTC_INVITE',
    WEBRTC_ACCEPT: 'WEBRTC_ACCEPT',
    WEBRTC_REJECT: 'WEBRTC_REJECT',
    WEBRTC_HANGUP: 'WEBRTC_HANGUP',
    WEBRTC_ROOM_JOIN_EVENT: 'WEBRTC_ROOM_JOIN_EVENT',
    WEBRTC_ROOM_REJECT_EVENT: 'WEBRTC_ROOM_REJECT_EVENT',
    WEBRTC_OFFER: 'offer',
    WEBRTC_ANSWER: 'answer',
    WEBRTC_CANDIDATE: 'candidate',
    WEBRTC_BYE: 'bye',


    COLLABORATIVE_CREATE : "COLLABORATIVE_CREATE",
    COLLABORATIVE_INVITE : "COLLABORATIVE_INVITE",
    COLLABORATIVE_JOIN : "COLLABORATIVE_JOIN",
    COLLABORATIVE_CONTEXT : "COLLABORATIVE_CONTEXT",
    COLLABORATIVE_COMMANDS : "COLLABORATIVE_COMMANDS",
    COLLABORATIVE_EXIT : "COLLABORATIVE_EXIT",
    COLLABORATIVE_KILL : "COLLABORATIVE_KILL",
    COLLABORATIVE_GIVE_HAND : "COLLABORATIVE_GIVE_HAND",
    COLLABORATIVE_KICK_USER : "COLLABORATIVE_KICK_USER",
    COLLABORATIVE_KICK_NOT_INVITED : "COLLABORATIVE_KICK_NOT_INVITED",
    COLLABORATIVE_WITHDRAW_INVITATION : "COLLABORATIVE_WITHDRAW_INVITATION",

    CHAT_MESSAGE: 'CHAT_MESSAGE',
    CHAT_MESSAGE_ACK: 'CHAT_MESSAGE_ACK',

    USER_STATUS : 'USER_STATUS'

};

var WEBRTC_CONFIG = {
    MS_TIMEOUT : 30000,
    PLAY_SOUND : true
};

var notificationSound = new buzz.sound(APP_CONFIG.contextPath + '/sounds/notification.ogg');
var incomingCallSound = new buzz.sound(APP_CONFIG.contextPath + '/sounds/incoming-call.ogg');

Backbone.Events.on('NotificationSound',function(){
    notificationSound.play();
});

Backbone.Events.on('IncomingCallSound',function(){
    if(WEBRTC_CONFIG.PLAY_SOUND){
        incomingCallSound.play();
    }
});

var CALL_STATE = {
    NO_CALL:    'NO_CALL',
    INCOMING:   'INCOMING',
    OUTGOING:   'OUTGOING',
    NEGOTIATING:'NEGOTIATING',
    RUNNING:    'RUNNING',
    ENDED:      'ENDED'
};

var REJECT_CALL_REASON = {
    REJECTED:'REJECTED',
    BUSY:'BUSY',
    TIMEOUT:'TIMEOUT',
    OFFLINE:'OFFLINE'
};


/*
 * Channel
 * 
 * create a new websocket
 **/

function Channel(url){
    this.status = ChannelStatus.CLOSED;
    this.url = url;
    this.listeners = [];
    this.create();
}

Channel.prototype = {
    
    create:function(){
        
        var self = this ;
        
        this.ws = new WebSocket(this.url);
        
        this.ws.onopen = function(event){
            console.log('Websocket created');
            self.onopen(event);
        };
        
        this.ws.onmessage = function(message){
            self.onmessage(message);            
        };
        
        this.ws.onclose = function(event){
            self.onclose(event);            
        };
        
        this.ws.onerror = function(event){
            self.onerror(event);            
        };
        
    },

    // send string
    send:function(message) {

        console.log('C->S: ' + message);
        this.ws.send(message);
        
    },

    // send object
    sendJSON:function(jsonObj) {
        
        var messageString = JSON.stringify(jsonObj);
        this.send(messageString);
        
    },    
    
    onopen:function(){

        this.status = ChannelStatus.OPENED;
        
        _.each(this.listeners,function(listener){
            listener.handlers.onStatusChanged(ChannelStatus.OPENED);            
        });
        
    },
    
    onmessage:function(message){

        console.log('S->C: ' + message.data);
        
        var jsonMessage = JSON.parse(message.data);
        if(jsonMessage.type){
            _.each(this.listeners,function(listener){
                if(listener.handlers.isApplicable(jsonMessage.type) && listener.isListening){
                    listener.handlers.onMessage(jsonMessage);
                }
            });
        }
        
    },
    
    onclose:function(event){
        this.status = ChannelStatus.CLOSED;

        console.log('Websocket closed');
        console.log(event);

        _.each(this.listeners,function(listener){            
            listener.handlers.onStatusChanged(ChannelStatus.CLOSED);
        });
        
    },
    
    onerror:function(event){
        console.log('Websocket error');
        console.log(event);
    },
    
    addChannelListener:function(listener){
        this.listeners.push(listener);
    },

    isReady : function(){
        return  this.status === ChannelStatus.OPENED;
    }

};

/*
 * ChannelListener 
 * 
 * listen to a channel, filtering on message types
 **/

function ChannelListener(handlers){
    this.handlers = handlers;
    this.isListening = true ;    
}

ChannelListener.prototype = {
    startListen:function(){
        this.isListening = true;
    },
    
    stopListen:function(){
        this.isListening = false;
    }
};
