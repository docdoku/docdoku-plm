define(function () {
	// TODO: use moments.js ?
	var formatTimestamp = function (format, timestamp) {
		try {
			var formated = new Date(timestamp).format(format);
			return formated;
		} catch (error) {
			console.error("app:formatDate(" + timestamp + ")", error);
			return timestamp;
		}
	}
	return {
		formatTimestamp: formatTimestamp
	}
});
