define([
    "require",
	"views/content",
	"views/workflows/workflow_list",
	"views/workflows/workflow_model_editor",
	"text!templates/workflows/workflow_content_list.html",
    "text!common-objects/templates/buttons/delete_button.html"
], function (
    require,
	ContentView,
	WorkflowListView,
    WorkflowEditorView,
	template,
    delete_button
) {
	var WorkflowContentListView = ContentView.extend({
        partials: {
            delete_button: delete_button
        },

		template: Mustache.compile(template),
		initialize: function () {
			ContentView.prototype.initialize.apply(this, arguments);
			this.events["click .actions .new"] = "actionNew";
			this.events["click .actions .delete"] = "actionDelete";
			this.events["click .actions .roles"] = "actionRoles";
		},
		rendered: function () {
			this.listView = this.addSubView(
				new WorkflowListView({
					el: "#list-" + this.cid
				})
			);
			this.listView.collection.fetch({reset:true});
			this.listView.on("selectionChange", this.selectionChanged);
			this.selectionChanged();
		},
		selectionChanged: function () {
			var showOrHide = this.listView.checkedViews().length > 0;
			var action = showOrHide ? "show" : "hide";
			this.$el.find(".actions .delete")[action]();
		},
		actionNew : function () {
            this.router = require("router").getInstance();
            this.router.navigate("workflow-model-editor", {trigger: true});
			return false;
		},
		actionDelete: function () {
			this.listView.eachChecked(function (view) {
				view.model.destroy();
			});
			return false;
		},

        actionRoles:function(){
            require(['views/workflows/roles_modal_view'], function(RolesModalView) {
                new RolesModalView().show();
            });
        }
	});
	return WorkflowContentListView;
});
