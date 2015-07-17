/*global define,mozRTCPeerConnection,mozRTCSessionDescription,mozRTCIceCandidate,webkitRTCPeerConnection,MediaStreamTrack,Promise*/
define(function () {

    'use strict';

    var RTCPeerConnection = null;
    var getUserMedia = null;
    var attachMediaStream = null;
    var reattachMediaStream = null;
    var webrtcDetectedBrowser = null;
    var webrtcDetectedVersion = null;
    var webrtcMinimumVersion = null;

    function requestUserMedia(constraints) {
        return new Promise(function (resolve, reject) {
            getUserMedia(constraints, resolve, reject);
        });
    }


    if (navigator.mozGetUserMedia) {
        webrtcDetectedBrowser = 'firefox';
        webrtcDetectedVersion = parseInt(navigator.userAgent.match(/Firefox\/([0-9]+)\./)[1], 10);
        webrtcMinimumVersion = 31;
        RTCPeerConnection = function (pcConfig, pcConstraints) {
            if (webrtcDetectedVersion < 38) {
                if (pcConfig && pcConfig.iceServers) {
                    var newIceServers = [];
                    for (var i = 0; i < pcConfig.iceServers.length; i++) {
                        var server = pcConfig.iceServers[i];
                        if (server.hasOwnProperty('urls')) {
                            for (var j = 0; j < server.urls.length; j++) {
                                var newServer = {url: server.urls[j]};
                                if (server.urls[j].indexOf('turn') === 0) {
                                    newServer.username = server.username;
                                    newServer.credential = server.credential;
                                }
                                newIceServers.push(newServer);
                            }
                        } else {
                            newIceServers.push(pcConfig.iceServers[i]);
                        }
                    }
                    pcConfig.iceServers = newIceServers;
                }
            }
            return new mozRTCPeerConnection(pcConfig, pcConstraints);
        };
        window.RTCSessionDescription = mozRTCSessionDescription;
        window.RTCIceCandidate = mozRTCIceCandidate;
        getUserMedia = webrtcDetectedVersion < 38 ? function (c, onSuccess, onError) {
            var constraintsToFF37 = function (c) {
                if (typeof c !== 'object' || c.require) {
                    return c;
                }
                var require = [];
                Object.keys(c).forEach(function (key) {
                    var r = c[key] = typeof c[key] === 'object' ? c[key] : {ideal: c[key]};
                    if (r.exact !== undefined) {
                        r.min = r.max = r.exact;
                        delete r.exact;
                    }
                    if (r.min !== undefined || r.max !== undefined) {
                        require.push(key);
                    }
                    if (r.ideal !== undefined) {
                        c.advanced = c.advanced || [];
                        var oc = {};
                        oc[key] = {min: r.ideal, max: r.ideal};
                        c.advanced.push(oc);
                        delete r.ideal;
                        if (!Object.keys(r).length) {
                            delete c[key];
                        }
                    }
                });
                if (require.length) {
                    c.require = require;
                }
                return c;
            };
            c.audio = constraintsToFF37(c.audio);
            c.video = constraintsToFF37(c.video);
            return navigator.mozGetUserMedia(c, onSuccess, onError);
        } : navigator.mozGetUserMedia.bind(navigator);
        navigator.getUserMedia = getUserMedia;
        if (!navigator.mediaDevices) {
            navigator.mediaDevices = {getUserMedia: requestUserMedia};
        }
        navigator.mediaDevices.enumerateDevices = navigator.mediaDevices.enumerateDevices || function () {
            return new Promise(function (resolve) {
                var infos = [{kind: 'audioinput', deviceId: 'default', label: '', groupId: ''}, {
                    kind: 'videoinput',
                    deviceId: 'default',
                    label: '',
                    groupId: ''
                }];
                resolve(infos);
            });
        };
        if (webrtcDetectedVersion < 41) {
            var orgEnumerateDevices = navigator.mediaDevices.enumerateDevices.bind(navigator.mediaDevices);
            navigator.mediaDevices.enumerateDevices = function () {
                return orgEnumerateDevices().catch(function (e) {
                    if (e.name === 'NotFoundError') {
                        return [];
                    }
                    throw e;
                });
            };
        }
        attachMediaStream = function (element, stream) {
            element.mozSrcObject = stream;
        };
        reattachMediaStream = function (to, from) {
            to.mozSrcObject = from.mozSrcObject;
        };
    } else {
        if (navigator.webkitGetUserMedia) {
            webrtcDetectedBrowser = 'chrome';
            webrtcDetectedVersion = parseInt(navigator.userAgent.match(/Chrom(e|ium)\/([0-9]+)\./)[2], 10);
            webrtcMinimumVersion = 38;
            RTCPeerConnection = function (pcConfig, pcConstraints) {
                return new webkitRTCPeerConnection(pcConfig, pcConstraints);
            };
            getUserMedia = function (c, onSuccess, onError) {
                var constraintsToChrome = function (c) {
                    if (typeof c !== 'object' || c.mandatory || c.optional) {
                        return c;
                    }
                    var cc = {};
                    Object.keys(c).forEach(function (key) {
                        if (key === 'require' || key === 'advanced') {
                            return;
                        }
                        var r = typeof c[key] === 'object' ? c[key] : {ideal: c[key]};
                        if (r.exact !== undefined && typeof r.exact === 'number') {
                            r.min = r.max = r.exact;
                        }
                        var oldname = function (prefix, name) {
                            if (prefix) {
                                return prefix + name.charAt(0).toUpperCase() + name.slice(1);
                            }
                            return name === 'deviceId' ? 'sourceId' : name;
                        };
                        if (r.ideal !== undefined) {
                            cc.optional = cc.optional || [];
                            var oc = {};
                            if (typeof r.ideal === 'number') {
                                oc[oldname('min', key)] = r.ideal;
                                cc.optional.push(oc);
                                oc = {};
                                oc[oldname('max', key)] = r.ideal;
                                cc.optional.push(oc);
                            } else {
                                oc[oldname('', key)] = r.ideal;
                                cc.optional.push(oc);
                            }
                        }
                        if (r.exact !== undefined && typeof r.exact !== 'number') {
                            cc.mandatory = cc.mandatory || {};
                            cc.mandatory[oldname('', key)] = r.exact;
                        } else {
                            ['min', 'max'].forEach(function (mix) {
                                if (r[mix] !== undefined) {
                                    cc.mandatory = cc.mandatory || {};
                                    cc.mandatory[oldname(mix, key)] = r[mix];
                                }
                            });
                        }
                    });
                    if (c.advanced) {
                        cc.optional = (cc.optional || []).concat(c.advanced);
                    }
                    return cc;
                };
                c.audio = constraintsToChrome(c.audio);
                c.video = constraintsToChrome(c.video);
                return navigator.webkitGetUserMedia(c, onSuccess, onError);
            };
            navigator.getUserMedia = getUserMedia;
            attachMediaStream = function (element, stream) {
                if (typeof element.srcObject !== 'undefined') {
                    element.srcObject = stream;
                } else {
                    if (typeof element.mozSrcObject !== 'undefined') {
                        element.mozSrcObject = stream;
                    } else {
                        if (typeof element.src !== 'undefined') {
                            element.src = window.URL.createObjectURL(stream);
                        } else {
                            console.error('Error attaching stream to element.');
                        }
                    }
                }
            };
            reattachMediaStream = function (to, from) {
                to.src = from.src;
            };
            if (!navigator.mediaDevices) {
                navigator.mediaDevices = {
                    getUserMedia: requestUserMedia,
                    enumerateDevices: function () {
                        return new Promise(function (resolve) {
                            var kinds = {audio: 'audioinput', video: 'videoinput'};
                            return MediaStreamTrack.getSources(function (devices) {
                                resolve(devices.map(function (device) {
                                    return {
                                        label: device.label,
                                        kind: kinds[device.kind],
                                        deviceId: device.id,
                                        groupId: ''
                                    };
                                }));
                            });
                        });
                    }
                };
            }
        } else {
            console.error('Browser does not appear to be WebRTC-capable');
        }
    }

    return {
        RTCPeerConnection: RTCPeerConnection,
        getUserMedia: getUserMedia,
        attachMediaStream: attachMediaStream,
        reattachMediaStream: reattachMediaStream,
        webrtcDetectedBrowser: webrtcDetectedBrowser,
        webrtcDetectedVersion: webrtcDetectedVersion,
        webrtcMinimumVersion: webrtcMinimumVersion
    };
});
