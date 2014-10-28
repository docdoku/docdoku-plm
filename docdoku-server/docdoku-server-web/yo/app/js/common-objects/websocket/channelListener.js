/*global define*/
'use strict';
define(function () {

    function ChannelListener(handlers) {
        this.handlers = handlers;
        this.isListening = true;
    }

    ChannelListener.prototype = {
        startListen: function () {
            this.isListening = true;
        },

        stopListen: function () {
            this.isListening = false;
        }
    };

    return ChannelListener;

});