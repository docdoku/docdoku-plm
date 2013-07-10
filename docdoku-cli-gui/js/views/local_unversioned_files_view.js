define(["text!templates/local_unversioned_files.html",
        "models/local_unversioned_file_model",
        "views/local_unversioned_file_view"],
    function(template, LocalUnVersionedFileModel, LocalUnVersionedFileView) {
    var LocalUnversionedFilesView = Backbone.View.extend({

        el: 'div#localRepo',

        template: Handlebars.compile(template),

        events: {
        },

        render:function() {
            this.$el.html(this.template({}));
            this.$unversionedFiles = this.$("#unversionedFiles");
            return this;
        },

        addUnversionedFile:function(file, path, stat) {
            var localUnVersionedFileModel = new LocalUnVersionedFileModel({name : file, path : path, mtime: stat.mtime});
            var localUnVersionedFileView =  new LocalUnVersionedFileView({model: localUnVersionedFileModel}).render();
            this.$unversionedFiles.append(localUnVersionedFileView.$el);
            return localUnVersionedFileView;
        }
    });

    return LocalUnversionedFilesView;
});