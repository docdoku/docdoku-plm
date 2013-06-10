//TODO rename the file to tag_collection
define([
	"models/tag"
], function (
	Tag
) {
	var TagList = Backbone.Collection.extend({
		model: Tag,

        className : "TagList",

		comparator: function (tagA, tagB) {
			// sort tags by label
			var labelA = tagA.get("label");
			var labelB = tagB.get("label");

			if (labelA == labelB){
                return 0;
            }
			return (labelA < labelB) ? -1 : 1;
		},


        url:function(){
            var baseUrl = "/api/workspaces/" + APP_CONFIG.workspaceId + "/tags";
            return baseUrl;
        },

        createTags : function(tags, callbackSuccess, callbackError){

            $.ajax({
                context: this,
                type: "POST",
                url: this.url()+"/multiple",
                data : JSON.stringify(tags),
                contentType: "application/json; charset=utf-8",
                success: function() {
                    this.fetch({reset:true});
                    callbackSuccess();
                },
                error:function(){
                    callbackError();
                }
            });

        }
	});

	return TagList;
});
