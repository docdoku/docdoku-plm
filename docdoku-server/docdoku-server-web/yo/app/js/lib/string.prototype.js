String.prototype.replaceUrl = function (target) {
    var exp = /(\b(https?|ftp|file):\/\/[-A-Z0-9+&@#\/%?=~_|!:,.;]*[-A-Z0-9+&@#\/%=~_|])/ig;
    if (target) {
        return this.replace(exp, "<a href='$1' target='" + target + "'>$1</a>");
    } else {
        return this.replace(exp, "<a href='$1'>$1</a>");
    }
};

String.prototype.nl2br = function () {
    return this.replace(/\n/g, "<br/>");
};