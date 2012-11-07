define([
	"views/components/modal",
	"views/document_new/document_new_attributes",
	"views/document_new/document_new_template_list",
	"views/document_new/document_new_workflow_list",
	"text!templates/document_new/document_new.html"
], function (
	ModalView,
	DocumentNewAttributesView,
	DocumentNewTemplateListView,
	DocumentNewWorkflowListView,
	template
) {
	var DocumentNewView = ModalView.extend({

		template: Mustache.compile(template),

		initialize: function () {
			ModalView.prototype.initialize.apply(this, arguments);
			this.events["submit #form-" + this.cid] = "primaryAction";
		},

		rendered: function () {
			this.attributesView = this.addSubView(
				new DocumentNewAttributesView({
					el: "#tab-attributes-" + this.cid
				})
			);
			this.attributesView.render();

			this.templatesView = this.addSubView(
				new DocumentNewTemplateListView({
					el: "#templates-" + this.cid,
					attributesView: this.attributesView
				})
			);
			this.templatesView.collection.fetch();

			this.workflowsView = this.addSubView(
				new DocumentNewWorkflowListView({
					el: "#workflows-" + this.cid
				})
			);
			this.workflowsView.collection.fetch();
		},

		primaryAction: function () {
			var reference = $("#form-" + this.cid + " .reference").val();

            if (reference) {
				var workflow = this.workflowsView.selected();
				var data = {
					reference: reference,
					title: $("#form-" + this.cid + " .title").val(),
					description: $("#form-" + this.cid + " .description").val(),
					workflowModelId: workflow ? workflow.get("id") : null
				};

				this.collection.create(data, {
					success: this.success,
					error: this.error
				});
			}

			return false;
		},

		success: function (model, response) {
			var that = this;
			model.lastIteration.save({
				instanceAttributes: this.attributesView.collection.toJSON()
			}, {
				success: function () {
					that.hide();
				},
				error: this.error
			});
			model.fetch();
		},

		error: function (model, error) {
			this.collection.remove(model);
			if (error.responseText) {
				this.alert({
					type: "error",
					message: error.responseText
				});
			} else {
				console.error(error);
			}
		}
	});
	return DocumentNewView;
});
