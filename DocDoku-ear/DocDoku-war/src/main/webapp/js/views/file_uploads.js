define([
	"views/base",
	"views/file_upload",
	"text!templates/file_uploads.html"
], function (
	BaseView,
	FileUploadView,
	template
) {
	var FileUploadsView = BaseView.extend({
		template: Mustache.compile(template),
		collection: function () {
			return new Backbone.Collection();
		},
		initialize: function () {
			BaseView.prototype.initialize.apply(this, arguments);
			this.events["click .add"] = this.addUpload;
		},
		addUpload: function () {
			var view = this.addSubView(
				new FileUploadView({
					model: this.model
				})
			).render();
			$("#items-" + this.cid).append(view.el);
		},
	});
	return FileUploadsView;
});
