//TODO rename the file to tag_collection
define([
	"models/tag"
], function (
	Tag
) {
	var TagList = Backbone.Collection.extend({
		model: Tag,
		comparator: function (tagA, tagB) {
			// sort tags by label
			var labelA = tagA.get("label");
			var labelB = tagB.get("label");

			if (labelA == labelB) return 0;
			return (labelA < labelB) ? -1 : 1;
		}
	});
	TagList.prototype.__defineGetter__("url", function () {
		var baseUrl = "/api/workspaces/" + APP_CONFIG.workspaceId + "/tags";
		return baseUrl;
	});
    TagList.className="TagList";
	return TagList;
});
