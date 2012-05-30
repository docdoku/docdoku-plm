define([
	"views/base",
	"text!templates/file_upload.html"
], function (
	BaseView,
	template
) {
	var FileUploadView = BaseView.extend({
		template: Mustache.compile(template),
		className: "list-item",
		initialize: function () {
			BaseView.prototype.initialize.apply(this, arguments);
			this.events["click .upload"] = this.upload;
			console.debug(this.model.fileUploadUrl() + "/test");
		},
		upload: function () {
			var xhr = new XMLHttpRequest();
			xhr.upload.addEventListener("progress", this.progress, false);
			xhr.addEventListener("load", this.load, false);
			xhr.addEventListener("error", this.error, false);
			xhr.addEventListener("abort", this.abort, false);
			xhr.open("POST", this.model.fileUploadUrl() + "/test.pdf");
			var form = document.getElementById("form-" + this.cid);
			var fd = new FormData(form);
<<<<<<< HEAD
=======
			console.debug(fd, form);
>>>>>>> webapp document edition file upload and css
			xhr.send(fd);
			return false;
		},
		progress: function (evt) {
			console.debug("progress", evt);
		},
		load: function (evt) {
			console.debug("load", evt);
		},
		error: function (evt) {
			console.debug("error", evt);
		},
		abort: function (evt) {
			console.debug("abort", evt);
		},
	});
	return FileUploadView;
});
