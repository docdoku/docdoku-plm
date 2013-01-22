define([
    "require",
	"views/checkbox_list_item",
	"text!templates/workflow_list_item.html"
], function (
    require,
	CheckboxListItemView,
	template
) {
	var WorkflowListItemView = CheckboxListItemView.extend({
		template: Mustache.compile(template),
		tagName: "tr",

        initialize: function(){
            CheckboxListItemView.prototype.initialize.apply(this, arguments);
            this.events["click .reference"] = this.actionEdit;
        },

        actionEdit: function (evt) {
            var router = require("router").getInstance();
            router.navigate("workflow-model-editor/"+this.model.id, {trigger: true});
        }

	});
	return WorkflowListItemView;
});
