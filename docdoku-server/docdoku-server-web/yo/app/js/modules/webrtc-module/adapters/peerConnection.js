/*global define,mozRTCPeerConnection,webkitRTCPeerConnection*/
define(function () {
    'use strict';
    var RTCPeerConnection = null;
    if (navigator.mozGetUserMedia) {
        RTCPeerConnection = mozRTCPeerConnection;
    } else if (navigator.webkitGetUserMedia) {
        RTCPeerConnection = webkitRTCPeerConnection;
    }
    return RTCPeerConnection;
});
