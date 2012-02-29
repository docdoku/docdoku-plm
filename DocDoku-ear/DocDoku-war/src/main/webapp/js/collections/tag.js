var TagList = Backbone.Collection.extend({
	model: Tag,
	comparator: function (tagA, tagB) {
		var labelA = tagA.get("label");
		var labelB = tagB.get("label");

		if (labelA == labelB) return 0;
		return (labelA < labelB) ? -1 : 1;
	}
});
TagList.prototype.__defineGetter__("url", function () {
	var baseUrl = "/api/workspaces/" + app.workspaceId + "/tags";
	return baseUrl;
});
