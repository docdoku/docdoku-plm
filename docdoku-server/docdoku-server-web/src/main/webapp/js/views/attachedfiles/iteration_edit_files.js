define([
	"views/base",
	"views/attachedfiles/file_upload",
	"text!templates/attachedfiles/iteration_edit_files.html",
    "i18n"
], function (
	BaseView,
	FileUploadView,
	template,
    i18n
) {
	var IterationEditFilesView = BaseView.extend({
		template: Mustache.compile(template),
		initialize: function () {
			BaseView.prototype.initialize.apply(this, arguments);
			this.events["click .add"] = this.addUpload;
            kumo.assertNotEmpty(this.model, "no model defined in IterationEditFilesView");
            kumo.assert (this.model.className=="DocumentIteration", "model should be a DocumentIteration");

            //list of further uploaded files view
			this.uploadViews = [];
		},

        getDocumentIteration : function(){
          return this.model;
        },

		addUpload: function () {
            //Add the view for uploading one file to the list of currently uploaded files
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
        render : function(){
            var data = {files :[], _:i18n, cid:this.cid}

            //current model is a DocumentIteration
            var files = this.model.get("attachedFiles");

            function build (file){
                data.files.push(
                    {
                        "shortName":file.getShortName(),
                        "fullName":file.getFullName()
                    }
                );
            }

            _.each(files.models, build);

            var elt = $(this.el);
            var html = this.template(data);
            elt.html(html);



        },
		deleteFiles: function () {
            var documentIteration =this.getDocumentIteration();
            var iterUrl = documentIteration.getUrl();

            //TODO we should declare events into view AttachedFileView and then call the AttachedFileModel
            var fileElements = this.$el.find(".file-delete:checked");
			$.each(fileElements, function (index, elt) {
                var elt = $(this);
				var shortName = elt.attr("value");
                var url = iterUrl+"/files/"+shortName;

				$.ajax(url, {
					type: "DELETE",
                    success : function(){

                        //remove from the dom
                        var liParent =elt.parents("li");
                        kumo.assert(liParent.hasClass("list-item"), "element parent is not a LI tag");
                        liParent.remove();

                        //remove from the collection
                        var fileCid = elt.id;
                        var existingFilesCollection = documentIteration.get("attachedFiles");
                        var fileModel = existingFilesCollection.getByCid(fileCid);
                        existingFilesCollection.remove(fileModel);


                    },
                    failure : function(resp){
                        console.error("error deleting "+shortName+" : "+resp);
                    }
				});
			});

            //Request are sent to the server. Now we remove it from the DOM
            this.$el.find(".file-delete:checked").hide();
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
		}
	});
	return IterationEditFilesView;
});
