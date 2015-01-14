/*global define*/
define(function () {
    'use strict';
    var getUserMedia = null;

    if (navigator.mozGetUserMedia) {
        getUserMedia = navigator.mozGetUserMedia.bind(navigator);
    } else if (navigator.webkitGetUserMedia) {
        getUserMedia = navigator.webkitGetUserMedia.bind(navigator);
    }

    return getUserMedia;

});
