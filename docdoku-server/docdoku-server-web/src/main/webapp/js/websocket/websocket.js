var ChannelStatus = {
    OPENED : "opened",
    CLOSED : "closed"
};

var ChannelMessagesType;

ChannelMessagesType = {

    WEBRTC_INVITE: "WEBRTC_INVITE",
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
    this.create();
    this.listeners = [];
    this.messageToSendOnOpen = messageToSendOnOpen;
}

Channel.prototype = {
    
    create:function(){
        
        var self = this ;

        //console.log("ws create : "+this.url);
        
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

        //console.log('C->S: ' + message);

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

        //console.log('S->C: ' + message.data);
        
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

// Set Opus as the default audio codec if it's present.
function preferOpus(sdp) {
    var sdpLines = sdp.split('\r\n');

    // Search for m line.
    for (var i = 0; i < sdpLines.length; i++) {
        if (sdpLines[i].search('m=audio') !== -1) {
            var mLineIndex = i;
            break;
        }
    }
    if (mLineIndex === null)
        return sdp;

    // If Opus is available, set it as the default in m line.
    for (var i = 0; i < sdpLines.length; i++) {
        if (sdpLines[i].search('opus/48000') !== -1) {
            var opusPayload = extractSdp(sdpLines[i], /:(\d+) opus\/48000/i);
            if (opusPayload)
                sdpLines[mLineIndex] = setDefaultCodec(sdpLines[mLineIndex], opusPayload);
            break;
        }
    }

    // Remove CN in m line and sdp.
    sdpLines = removeCN(sdpLines, mLineIndex);

    sdp = sdpLines.join('\r\n');
    return sdp;
}

function extractSdp(sdpLine, pattern) {
    var result = sdpLine.match(pattern);
    return (result && result.length == 2)? result[1]: null;
}

// Set the selected codec to the first in m line.
function setDefaultCodec(mLine, payload) {
    var elements = mLine.split(' ');
    var newLine = new Array();
    var index = 0;
    for (var i = 0; i < elements.length; i++) {
        if (index === 3) // Format of media starts from the fourth.
            newLine[index++] = payload; // Put target payload to the first.
        if (elements[i] !== payload)
            newLine[index++] = elements[i];
    }
    return newLine.join(' ');
}

// Strip CN from sdp before CN constraints is ready.
function removeCN(sdpLines, mLineIndex) {
    var mLineElements = sdpLines[mLineIndex].split(' ');
    // Scan from end for the convenience of removing an item.
    for (var i = sdpLines.length-1; i >= 0; i--) {
        var payload = extractSdp(sdpLines[i], /a=rtpmap:(\d+) CN\/\d+/i);
        if (payload) {
            var cnPos = mLineElements.indexOf(payload);
            if (cnPos !== -1) {
                // Remove CN payload from m line.
                mLineElements.splice(cnPos, 1);
            }
            // Remove CN line in sdp
            sdpLines.splice(i, 1);
        }
    }

    sdpLines[mLineIndex] = mLineElements.join(' ');
    return sdpLines;
}