define(["text!templates/local_versioned_files.html",
        "i18n!localization/nls/global",
        "views/loader",
        "models/local_versioned_file",
        "views/local_versioned_file"
        ], function(template, i18n, Loader, LocalVersionedFileModel, LocalVersionedFileView) {
    var LocalVersionedFilesView = Backbone.View.extend({

        el: 'div#localRepo',

        template: Handlebars.compile(template),

        events: {},

        render:function() {
            this.$el.append(this.template({i18n:i18n}));
            this.$versionedFiles = this.$("#versionedFiles");
            this.$loader = new Loader();
            this.$versionedFiles.append(this.$loader);
            return this;
        },

        addVersionedFile:function(file,path,status,stat){
            var localVersionedFileModel = new LocalVersionedFileModel({name : file, path : path, status: status, mtime: stat.mtime});
            var localVersionedFileView  = new LocalVersionedFileView({model: localVersionedFileModel}).render();
            this.$versionedFiles.append(localVersionedFileView.$el);
        },

        removeLoader:function(){
            this.$loader.remove();
        }
    });

    return LocalVersionedFilesView;
});