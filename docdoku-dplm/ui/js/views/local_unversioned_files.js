define(["text!templates/local_unversioned_files.html",
        "i18n!localization/nls/global",
        "views/loader",
        "models/local_unversioned_file",
        "views/local_unversioned_file"],
    function(template, i18n, Loader, LocalUnVersionedFileModel, LocalUnVersionedFileView) {
    var LocalUnversionedFilesView = Backbone.View.extend({

        el: 'div#localRepo',

        template: Handlebars.compile(template),

        events: {},

        render:function() {
            this.$el.append(this.template({i18n:i18n}));
            this.$unversionedFiles = this.$("#unversionedFiles");
            this.$loader = new Loader();
            this.$unversionedFiles.append(this.$loader);
            return this;
        },

        addUnversionedFile:function(file, path, stat) {
            var localUnVersionedFileModel = new LocalUnVersionedFileModel({name : file, path : path, mtime: stat.mtime});
            var localUnVersionedFileView =  new LocalUnVersionedFileView({model: localUnVersionedFileModel}).render();
            this.$unversionedFiles.append(localUnVersionedFileView.$el);
            return localUnVersionedFileView;
        },

        removeLoader:function(){
            this.$loader.remove();
        }
    });

    return LocalUnversionedFilesView;
});