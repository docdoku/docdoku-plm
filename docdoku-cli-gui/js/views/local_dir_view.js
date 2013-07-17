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

        setPath:function(path){
            this.path = path;
            return this;
        },

        render:function() {

            var p = this.path;

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

            function removeLoaders() {
                self.localVersionedFilesView.removeLoader();
                self.localUnVersionedFilesView.removeLoader();
            }

            wrench.readdirRecursiveFull(p, function(error, curFiles) {
                var totalFiles = curFiles.length;
                if(totalFiles == 0) {
                    removeLoaders();
                }
                _.each(curFiles, function (file) {
                    fs.stat(path.join(p, file), function(err, stat) {
                        totalFiles--;
                        if(stat.isFile() && file !== ".dplm/index.xml") {
                            Commander.getStatusForFile(path.join(p, file), function(pStatus) {
                                var status = JSON.parse(pStatus);

                                if (!status.dplmError) {
                                    self.localVersionedFilesView.addVersionedFile(file,p,status,stat);
                                } else {
                                    var localUnVersionedFileView = self.localUnVersionedFilesView.addUnversionedFile(file, p, stat);
                                    self.addPartCreatedListener(localUnVersionedFileView,file, p, stat);
                                }
                                if(totalFiles <= 0) {
                                    removeLoaders();
                                }
                            });
                        }
                    });
                });
            });
        },

        addPartCreatedListener:function(localUnVersionedFileView,file, p, stat) {
            var self = this ;
            localUnVersionedFileView.on("part:created", function(model) {
                Commander.getStatusForFile(path.join(p, file), function(pStatus) {
                    var status = JSON.parse(pStatus);
                    if (!status.statusError) {
                        self.localVersionedFilesView.addVersionedFile(file,p,status,stat);
                        localUnVersionedFileView.remove();
                    }
                });
            })
        }
    });

    return LocalRepoView;
});