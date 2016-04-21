String.prototype.replaceUrl = function () {
	'use strict';
    var exp = /(\b(https?|ftp|file):\/\/[\-A-Z0-9+&@#\/%?=~_|!:,.;]*[\-A-Z0-9+&@#\/%=~_|])/ig;
    return this.replace(exp, '<a href="$1" target="_blank">$1</a>');
};

String.prototype.nl2br = function () {
	'use strict';
    return this.replace(/\n/g, '<br/>');
};
