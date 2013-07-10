define(["text!templates/local_unversioned_files.html",
        "i18n!localization/nls/global",
        "models/local_unversioned_file_model",
        "views/local_unversioned_file_view"],
    function(template, i18n, LocalUnVersionedFileModel, LocalUnVersionedFileView) {
    var LocalUnversionedFilesView = Backbone.View.extend({

        el: 'div#localRepo',

        template: Handlebars.compile(template),

        events: {},

        render:function() {
            this.$el.append(this.template({i18n:i18n}));
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