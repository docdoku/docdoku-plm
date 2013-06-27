define([
    "models/versioned_file_model",
    "models/unversioned_file_model",
    "views/versioned_file_view",
    "views/unversioned_file_view",
    "storage",
    "commander",
    "views/loader_view"], function(VersionedFileModel, UnVersionedFileModel, VersionedFileView, UnVersionedFileView, Storage, Commander, Loader) {
    var LocalRepoView = Backbone.View.extend({

        events: {},

        render:function() {
            var self = this;
            var p = Storage.getDirectory();

            this.$versionedFiles = this.$("#versionedFiles");
            this.$unVersionedFiles = this.$("#unVersionedFiles");

            this.$versionedFiles.empty();
            this.$unVersionedFiles.empty();

            wrench.readdirRecursiveFull(p, function(error, curFiles) {

                _.each(curFiles, function (file) {
                    fs.stat(path.join(p, file), function(err, stat) {
                        if(stat.isFile() && file !== ".dplm/index.xml") {
                            Commander.getStatusForFile(path.join(p, file), function(pStatus) {
                                var status = JSON.parse(pStatus);
                                if (!status.statusError) {
                                    var versionedFileModel = new VersionedFileModel({name : file, path : p, status: status, mtime: stat.mtime});
                                    var versionedFileView =  new VersionedFileView({model: versionedFileModel}).render();
                                    self.$versionedFiles.append(versionedFileView.$el);
                                } else {
                                    var unVersionedFileModel = new UnVersionedFileModel({name : file, path : p, mtime: stat.mtime});
                                    var unVersionedFileView =  new UnVersionedFileView({model: unVersionedFileModel}).render();
                                    unVersionedFileView.on("part:created", function() {
                                        Commander.getStatusForFile(path.join(p, file), function(pStatus) {
                                            var status = JSON.parse(pStatus);
                                            if (!status.statusError) {
                                                var versionedFileModel = new VersionedFileModel({name : file, path : p, status: status, mtime: stat.mtime});
                                                var versionedFileView =  new VersionedFileView({model: versionedFileModel}).render();
                                                self.$versionedFiles.append(versionedFileView.$el);
                                                unVersionedFileView.remove();
                                            } else {

                                            }
                                        });

                                    })
                                    self.$unVersionedFiles.append(unVersionedFileView.$el);
                                }
                            });
                        }
                    });
                });
            });
        }
    });

    return LocalRepoView;
});