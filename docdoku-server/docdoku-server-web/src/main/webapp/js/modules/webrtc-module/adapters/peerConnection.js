define(function(){
    var RTCPeerConnection= null;
    if (navigator.mozGetUserMedia) {
        RTCPeerConnection = mozRTCPeerConnection;
    } else if (navigator.webkitGetUserMedia) {
        RTCPeerConnection = webkitRTCPeerConnection;
    }
    return RTCPeerConnection;
});
