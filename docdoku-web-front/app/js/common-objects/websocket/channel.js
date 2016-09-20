/*global _,define,WebSocket,App*/
define(['common-objects/websocket/channelStatus','common-objects/log'], function (ChannelStatus,Logger) {
	'use strict';
    function Channel() {
        this.status = ChannelStatus.CLOSED;
        this.listeners = [];
    }

    Channel.prototype = {

        init: function (url) {
            this.url = url;
            this.create();
        },

        create: function () {
            var self = this;

            this.ws = new WebSocket(this.url);

            this.ws.onopen = function (event) {
                Logger.log('%c Websocket created','WS');
                self.onopen(event);
            };

            this.ws.onmessage = function (message) {
                self.onmessage(message);
            };

            this.ws.onclose = function (event) {
                self.onclose(event);
            };

            this.ws.onerror = function (event) {
                self.onerror(event);
            };

        },

        // send string
        send: function (message) {

            Logger.log('C->S: %c' + message,'WS');
            this.ws.send(message);

        },

        // send object
        sendJSON: function (jsonObj) {

            var messageString = JSON.stringify(jsonObj);
            this.send(messageString);

        },

        onopen: function () {

            this.status = ChannelStatus.OPENED;

            _.each(this.listeners, function (listener) {
                listener.handlers.onStatusChanged(ChannelStatus.OPENED);
            });

        },

        onmessage: function (message) {
            Logger.log('S->C: %c' + message.data,'WS');

            var jsonMessage = JSON.parse(message.data);
            if (jsonMessage.type) {
                _.each(this.listeners, function (listener) {
                    if (listener.handlers.isApplicable(jsonMessage.type) && listener.isListening) {
                        listener.handlers.onMessage(jsonMessage);
                    }
                });
            }

        },

        onclose: function (event) {
            this.status = ChannelStatus.CLOSED;

            Logger.log('%c Websocket closed\n\t'+event,'WS');

            _.each(this.listeners, function (listener) {
                listener.handlers.onStatusChanged(ChannelStatus.CLOSED);
            });

        },

        onerror: function (event) {
            Logger.log('%c Websocket error\n\t'+event,'WS');
        },

        addChannelListener: function (listener) {
            this.listeners.push(listener);
        },

        isReady: function () {
            return  this.status === ChannelStatus.OPENED;
        }

    };

    return Channel;

});
