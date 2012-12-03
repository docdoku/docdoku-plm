/**
 * Created with IntelliJ IDEA.
 * User: yannsergent
 * Date: 13/11/12
 * Time: 13:13
 * To change this template use File | Settings | File Templates.
 */

define([
    "views/content",
    "text!templates/workflow_content_new.html"
], function (
    ContentView,
    template
    ) {
    var WorkflowContentNewView = ContentView.extend({
        template: Mustache.compile(template),
        tagName: "div",
        initialize: function () {
            ContentView.prototype.initialize.apply(this, arguments);
            this.events["click .actions .cancel"] = "cancelAction";
            this.events["click .actions .btn-primary"] = "saveAction";
            this.initWorkflowEditor();
        },
        cancelAction: function () {
            return false;
        },
        saveAction: function () {
            this.nameInput = this.$el.find("input.name").first();
            var name = this.nameInput.val();
            if (name) {
                this.collection.create({
                    reference: name,
                    finalLifeCycleState: "totoFinalCycle"

                }, {
                    success: this.success,
                    error: this.error
                });
            }
            return false;
        },
        success: function () {
            this.hide();
            this.parentView.show();
        },
        error: function (model, error) {
            if (error.responseText) {
                this.alert({
                    type: "error",
                    message: error.responseText
                });
            } else {
                console.error(error);
            }
        },

        initWorkflowEditor: function(){

        }
    });
    return WorkflowContentNewView;
});