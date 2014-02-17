define([
    "require",
	"views/content",
	"views/workflow_list",
	"views/workflow_model_editor",
	"text!templates/workflow_content_list.html"
], function (
    require,
	ContentView,
	WorkflowListView,
    WorkflowEditorView,
	template
) {
	var WorkflowContentListView = ContentView.extend({
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
            require(['views/roles_modal_view'], function(RolesModalView) {
                new RolesModalView().show();
            });
        }
	});
	return WorkflowContentListView;
});
