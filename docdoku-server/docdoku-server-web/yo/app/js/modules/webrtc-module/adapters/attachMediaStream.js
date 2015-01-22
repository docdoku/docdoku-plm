/*global define,webkitURL*/
define(function () {
    'use strict';
    var attachMediaStream = null;
    if (navigator.mozGetUserMedia) {
        attachMediaStream = function (element, stream) {
            element.mozSrcObject = stream;
            element.play();
        };
    } else if (navigator.webkitGetUserMedia) {
        attachMediaStream = function (element, stream) {
            element.src = webkitURL.createObjectURL(stream);
        };
    }
    return attachMediaStream;
});
