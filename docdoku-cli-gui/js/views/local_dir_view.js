define([
    "text!templates/local_repository.html",
    "views/local_versioned_files_view",
    "views/local_unversioned_files_view",
    "storage",
    "commander",
    "views/loader_view"], function(template, LocalVersionedFilesView, LocalUnVersionedFilesView, Storage, Commander, Loader) {
    var LocalRepoView = Backbone.View.extend({

        el:"div#subContent",

        template: Handlebars.compile(template),

        events: {},

        render:function() {

            var p = Storage.getDirectory();

            // render view on subContent
            this.$el.html(this.template());

            //Create local_versioned_files_view
            this.localVersionedFilesView = new LocalVersionedFilesView();
            this.localVersionedFilesView.render();

            //Create local_unversioned_files_view
            this.localUnVersionedFilesView = new LocalUnVersionedFilesView();
            this.localUnVersionedFilesView.render();

            //this.$versionedFiles.empty();
            //this.$unVersionedFiles.empty();

            var self = this;

            wrench.readdirRecursiveFull(p, function(error, curFiles) {

                _.each(curFiles, function (file) {
                    fs.stat(path.join(p, file), function(err, stat) {
                        if(stat.isFile() && file !== ".dplm/index.xml") {
                            Commander.getStatusForFile(path.join(p, file), function(pStatus) {
                                var status = JSON.parse(pStatus);

                                if (!status.statusError) {
                                    self.localVersionedFilesView.addVersionedFile(file,p,status,stat);
                                } else {
                                    var localUnVersionedFileView = self.localUnVersionedFilesView.addUnversionedFile(file, p, stat);
                                    self.addPartCreatedListener(localUnVersionedFileView);
                                }
                            });
                        }
                    });
                });
            });
        },

        addPartCreatedListener:function(localUnVersionedFileView, p, stat) {
            localUnVersionedFileView.on("part:created", function() {
                Commander.getStatusForFile(path.join(p, file), function(pStatus) {
                    var status = JSON.parse(pStatus);
                    if (!status.statusError) {
                        this.localVersionedFilesView.addVersionedFile(file,p,status,stat);
                        localUnVersionedFileView.remove();
                    } else {}
                });
            })
        }
    });

    return LocalRepoView;
});