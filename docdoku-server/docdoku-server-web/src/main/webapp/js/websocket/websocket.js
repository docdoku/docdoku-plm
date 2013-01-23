var ChannelStatus = {
    OPENED : "opened",
    CLOSED : "closed"
};

var ChannelMessagesType = {

    USER_STATUS : "USER_STATUS",
    WEBRTC_INVITE: "WEBRTC_INVITE",
    WEBRTC_INVITE_TIMEOUT: "WEBRTC_INVITE_TIMEOUT",
    WEBRTC_ACCEPT: "WEBRTC_ACCEPT",
    WEBRTC_REJECT: "WEBRTC_REJECT",
    WEBRTC_HANGUP: "WEBRTC_HANGUP",

    WEBRTC_OFFER: "offer",
    WEBRTC_ANSWER: "answer",
    WEBRTC_CANDIDATE: "candidate",
    WEBRTC_BYE: "bye",

    CHAT_MESSAGE: "CHAT_MESSAGE"
};

/*
 * Channel
 * 
 * create a new websocket
 **/

function Channel(url,messageToSendOnOpen){
    this.url = url;
    this.listeners = [];
    this.messageToSendOnOpen = messageToSendOnOpen;
    this.create();
}

Channel.prototype = {
    
    create:function(){
        
        var self = this ;
        
        this.ws = new WebSocket(this.url);
        
        this.ws.onopen = function(event){
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

        var sent = this.ws.send(message);
        
        if(!sent){
            //console.log("ws not sent ! : "+message);
        }
        
    },

    // send object
    sendJSON:function(jsonObj) {
        
        var messageString = JSON.stringify(jsonObj);
        this.send(messageString);
        
    },    
    
    onopen:function(event){

        //console.log("ws onopen");
        
        if(this.messageToSendOnOpen){            
            this.send(this.messageToSendOnOpen);
        }
        
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

        //console.log("ws onclose");
        
        _.each(this.listeners,function(listener){            
            listener.handlers.onStatusChanged(ChannelStatus.CLOSED);            
        });
        
    },
    
    onerror:function(event){

        //console.log("ws onerror");
        //console.log(event);
        
    },
    
    addChannelListener:function(listener){
        
        this.listeners.push(listener);
        
    },
    
    removeAllChannelListeners : function(){
        
        _.each(this.listeners,function(listener){            
            listener.handlers.onChannelOver();            
        }); 
        
        this.listeners = [];
        
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
