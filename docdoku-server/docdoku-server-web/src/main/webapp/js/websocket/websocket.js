var ChannelStatus = {
    OPENED : "opened",
    CLOSED : "closed"
};

var ChannelMessagesType = {
    WEBRTC_INVITE :"webRTC_invite",
    CHAT_MESSAGE :"chat_message"
};

/*
 * Channel
 * 
 * create a new websocket
 **/

function Channel(url,messageToSendOnOpen){
    this.url = url;
    this.create();
    this.listeners = [];
    this.messageToSendOnOpen = messageToSendOnOpen;
}

Channel.prototype = {
    
    create:function(){
        
        var self = this ;

        console.log("ws create : "+this.url);
        
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
    
    send:function(message) {

        console.log("ws send");
        
        var sent = this.ws.send(message);
        
        if(!sent){
            console.log("ws not sent ! : "+message);
        }
        
    },
    
    sendJSON:function(jsonObj) {
        
        var messageString = JSON.stringify(jsonObj);
        
        this.send(messageString);
        
    },    
    
    onopen:function(event){

        console.log("ws onopen");
        
        if(this.messageToSendOnOpen){            
            this.send(this.messageToSendOnOpen);
        }
        
        _.each(this.listeners,function(listener){                        
            listener.handlers.onStatusChanged(ChannelStatus.OPENED);            
        });         
        
    },
    
    onmessage:function(message){

        console.log("ws onmessage");
        console.log(message);
        
        var jsonMessage = JSON.parse(message.data);
        
        _.each(this.listeners,function(listener){     
            if(listener.handlers.isApplicable(jsonMessage.type) && listener.isListening){
                listener.handlers.onMessage(jsonMessage);
            }
        });
        
    },
    
    onclose:function(event){

        console.log("ws onclose");
        
        _.each(this.listeners,function(listener){            
            listener.handlers.onStatusChanged(ChannelStatus.CLOSED);            
        });
        
    },
    
    onerror:function(event){

        console.log("ws onerror");
        console.log(event);
        
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
 * Usage :
    var listener = new ChannelListener({
        isApplicable:function(messageType){
            return messageType == ChannelMessagesType.MESSAGE_TYPE_NEEDED;
        },
        onMessage : function(message){
             console.log(message);
        },
        onStatusChanged:function(status){      
             console.log(status);
        },
        onChannelOver:function(){
            // Do what you want
        }
    });
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