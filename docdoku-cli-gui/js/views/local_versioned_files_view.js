define(["text!templates/local_versioned_files.html",
        "i18n!localization/nls/global",
        "models/local_versioned_file_model",
        "views/local_versioned_file_view"
        ], function(template, i18n, LocalVersionedFileModel, LocalVersionedFileView) {
    var LocalVersionedFilesView = Backbone.View.extend({

        el: 'div#localRepo',

        template: Handlebars.compile(template),

        events: {},

        render:function() {
            this.$el.append(this.template({i18n:i18n}));
            this.$versionedFiles = this.$("#versionedFiles");
            return this;
        },

        addVersionedFile:function(file,path,status,stat){
            var localVersionedFileModel = new LocalVersionedFileModel({name : file, path : path, status: status, mtime: stat.mtime});
            var localVersionedFileView  = new LocalVersionedFileView({model: localVersionedFileModel}).render();
            this.$versionedFiles.append(localVersionedFileView.$el);
        }
    });

    return LocalVersionedFilesView;
});