/*global APP_CONFIG*/
define([
    "require",
    "views/content",
    "views/workflows/workflow_list",
    "views/workflows/workflow_model_editor",
    "text!templates/workflows/workflow_content_list.html",
    "text!common-objects/templates/buttons/delete_button.html",
    'views/workflows/roles_modal_view'
], function (require, ContentView, WorkflowListView, WorkflowEditorView, template, deleteButton, RolesModalView) {
    var WorkflowContentListView = ContentView.extend({

        template: template,

        partials: {
            deleteButton: deleteButton
        },

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
            this.listView.collection.fetch({reset: true});
            this.listView.on("selectionChange", this.selectionChanged);
            this.selectionChanged();
        },
        selectionChanged: function () {
            var showOrHide = this.listView.checkedViews().length > 0;
            var action = showOrHide ? "show" : "hide";
            this.$el.find(".actions .delete")[action]();
        },
        actionNew: function () {
            App.router.navigate(APP_CONFIG.workspaceId + "/workflow-model-editor", {trigger: true});
            return false;
        },
        actionDelete: function () {
            this.listView.eachChecked(function (view) {
                view.model.destroy();
            });
            return false;
        },

        actionRoles: function () {
            new RolesModalView().show();
        }
    });
    return WorkflowContentListView;
});
