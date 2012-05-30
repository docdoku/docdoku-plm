define([
	"views/base",
	"views/file_upload",
	"text!templates/iteration_edit_files.html"
], function (
	BaseView,
	FileUploadView,
	template
) {
	var IterationEditFilesView = BaseView.extend({
		template: Mustache.compile(template),
		initialize: function () {
			BaseView.prototype.initialize.apply(this, arguments);
			this.events["click .add"] = this.addUpload;
			this.uploadViews = [];
		},
		addUpload: function () {
			var view = this.addSubView(
				new FileUploadView({
					model: this.model
				})
			).render();
			$("#file-uploads-" + this.cid).append(view.el);
			this.uploadViews.push(view);
		},
		save: function (options) {
			this.deleteFiles();
			this.uploadFiles();
		},
		deleteFiles: function () {
			this.$el.find(".file-delete").each(function () {
				var url = $(this).attr("value");
				$.ajax(url, {
					type: "DELETE"
				});
			});
		},
		uploadFiles: function () {
			this.finishedUploads = [];
			var that = this;

			_.map(this.uploadViews, function (view) {
				view.bind("success", that.uploadSuccess);
				view.bind("error", that.uploadError);
				view.bind("finished", that.uploadFinished);
				view.upload();
			});
		},
		uploadFinished: function (evt) {
			this.finishedUploads.push(evt);
			if (this.finishedUploads.length == this.uploadViews.length) {
				_.map(this.uploadViews, function (view) {
					view.destroy();
				});
				this.trigger("saved");
			}
		},
		uploadSuccess: function (evt) {
		},
		uploadError: function (evt) {
		},
	});
	return IterationEditFilesView;
});
