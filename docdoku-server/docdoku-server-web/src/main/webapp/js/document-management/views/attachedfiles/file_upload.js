define([
    "i18n",
	"views/base",
	"text!templates/attachedfiles/file_upload.html"
], function (
    i18n,
	BaseView,
	template
) {
	var FileUploadView = BaseView.extend({
		template: Mustache.compile(template),
		tagName:  "li",
		className: "list-item",
		initialize: function () {
			BaseView.prototype.initialize.apply(this, arguments);


			this.events["click .upload"] = this.upload;
			this.events["click .remove"] = this.destroy;
		},
        /*
        render : function(){
            var data = {};
            data.i18n = i18n;
            data.files = [];
            //we build a map with {"fullName":"shortName"}
            //_.each

        },*/

		upload: function () {
			var form = document.getElementById("form-" + this.cid);
			if (form.upload.value) {
				var shortName = form.upload.value.split(/(\\|\/)/g).pop();
				var xhr = new XMLHttpRequest();
				xhr.upload.addEventListener("progress", this.progress, false);
				xhr.addEventListener("load", this.load, false);
				xhr.addEventListener("error", this.error, false);
				xhr.addEventListener("abort", this.abort, false);
				xhr.open("POST", this.model.getUploadUrl(shortName));

                var files = $('input[type=file]');
                var fd = new FormData();

                for (var i = 0 ; i <files.length ; i++){
                    var fileId = files[i].id;
                    var html5File = document.getElementById(fileId);
                    fd.append("upload", html5File.files[0]);
                }

				xhr.send(fd);
			} else {
				// Nothing to upload
				this.trigger("finished", evt, this);
			}
			return false;
		},
		progress: function (evt) {
			console.debug("progress", evt, this);
		},
		load: function (evt) {
            //on success
			this.trigger("success", evt, this);
			this.trigger("finished", evt, this);
		},
		error: function (evt) {
			this.trigger("error", evt, this);
			this.trigger("finished", evt, this);
		},
		abort: function (evt) {
			this.trigger("finished", evt, this);
		}
	});
	return FileUploadView;
});
