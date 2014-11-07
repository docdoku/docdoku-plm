String.prototype.replaceUrl = function (target) {
	'use strict';
    var exp = /(\b(https?|ftp|file):\/\/[-A-Z0-9+&@#\/%?=~_|!:,.;]*[-A-Z0-9+&@#\/%=~_|])/ig;
    if (target) {
        return this.replace(exp, '<a href="$1" target="' + target + ' ">$1</a>');
    } else {
        return this.replace(exp, '<a href="$1">$1</a>');
    }
};

String.prototype.nl2br = function () {
	'use strict';
    return this.replace(/\n/g, '<br/>');
};