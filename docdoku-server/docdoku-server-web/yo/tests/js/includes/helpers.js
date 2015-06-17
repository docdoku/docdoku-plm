/*jshint -W098*/

var helpers = {
    findReasonInResponseHeaders: function (headers) {
        'use strict';
        var reason = '';
        headers.forEach(function (header) {
            if (header.name === 'Reason-Phrase') {
                reason = header.value;
            }
        });
        return reason;
    }
};
