define([
	"views/base",
	"views/file_uploads",
	"text!templates/iteration_edit_files.html"
], function (
	BaseView,
	FileUploadsView,
	template
) {
	var IterationEditFilesView = BaseView.extend({
		template: Mustache.compile(template),
		collection: function () {
			return new Backbone.Collection();
		},
		initialize: function () {
			BaseView.prototype.initialize.apply(this, arguments);
		},
		rendered: function () {
			this.fileUploadsView = this.addSubView(
				new FileUploadsView({
					el: "#file-uploads-" + this.cid,
					model: this.model
				})
			);
			this.fileUploadsView.render();
		},
	});
	return IterationEditFilesView;
});
