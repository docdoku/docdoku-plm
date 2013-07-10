define(["text!templates/local_versioned_files.html",
        "models/local_versioned_file_model",
        "views/local_versioned_file_view"
        ], function(template, LocalVersionedFileModel, LocalVersionedFileView) {
    var LocalVersionedFilesView = Backbone.View.extend({

        el: 'div#localRepo',

        template: Handlebars.compile(template),

        events: {
        },

        render:function() {
            this.$el.html(this.template({}));
            this.$versionedFiles = this.$("#versionedFiles");
            return this;
        },

        addVersionedFile:function(file,path,status,stat){
            var localVersionedFileModel = new LocalVersionedFileModel({name : file, path : path, status: status, mtime: stat.mtime});
            var localVersionedFileView =  new LocalVersionedFileView({model: localVersionedFileModel}).render();
            this.$versionedFiles.append(localVersionedFileView.$el);
        }
    });

    return LocalVersionedFilesView;
});