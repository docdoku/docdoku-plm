define([
	"models/part_template"
], function (
	Template
) {
	var TemplateList = Backbone.Collection.extend({

		model: Template,

        url: function(){
            return "/api/workspaces/" + APP_CONFIG.workspaceId + "/part-templates";
        }

    });

	return TemplateList;
});
