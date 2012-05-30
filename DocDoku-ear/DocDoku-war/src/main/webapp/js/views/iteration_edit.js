define([
	"i18n",
	"common/date",
	"views/base",
	"views/document_new_attributes",
<<<<<<< HEAD
	"views/iteration_edit_files",
=======
	"views/file_uploads",
>>>>>>> webapp document edition file upload and css
	"text!templates/iteration_edit.html"
], function (
	i18n,
	date,
	BaseView,
	DocumentNewAttributesView,
<<<<<<< HEAD
	IterationEditFilesView,
=======
	FileUploadsView,
>>>>>>> webapp document edition file upload and css
	template
) {
	var IterationEditView = BaseView.extend({
		className: "document-edit",
		template: Mustache.compile(template),
		initialize: function () {
			BaseView.prototype.initialize.apply(this, arguments);
			this.events["click footer .cancel"] = "cancelAction";
			this.events["click footer .btn-primary"] = "primaryAction";
		},
		modelToJSON: function () {
			var data = this.model.toJSON();

			// Format dates
			if (data.creationDate) {
				data.creationDate = date.formatTimestamp(
					i18n._DATE_FORMAT,
					data.creationDate);
			}
			return data;
		},
		rendered: function () {
			this.attributesView = this.addSubView(
				new DocumentNewAttributesView({
					el: "#attributes-" + this.cid,
				})
			);
			this.attributesView.render();
			this.attributesView.collection.reset(this.model.get("instanceAttributes"));

<<<<<<< HEAD
			this.filesView = this.addSubView(
				new IterationEditFilesView({
					el: "#files-" + this.cid,
					model: this.model
				})
			);
			this.filesView.render();
=======
			this.fileUploadsView = this.addSubView(
				new FileUploadsView({
					el: "#file-uploads-" + this.cid,
					model: this.model
				})
			);
			this.fileUploadsView.render();
>>>>>>> webapp document edition file upload and css
		},
		cancelAction: function () {
			this.render();
			return false;
		},
		primaryAction: function () {
			var revisionNote = $("#form-" + this.cid + " .revision-note").val();
			this.model.save({
				revisionNote: revisionNote,
				instanceAttributes: this.attributesView.collection.toJSON()
			}, {
				success: this.success,
				error: this.error
			});
			return false;
		},
		success: function (model, response) {
			this.alert({
				type: "",
				title: i18n["SAVED"],
				message: ""
			});
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
		}
	});
	return IterationEditView;
});
