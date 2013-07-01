define([
    "models/remote_versioned_file_model",
    "views/remote_versioned_file_view",
    "commander"],
    function(RemoteVersionedFileModel, RemoteVersionedFileView, Commander) {
    var WorkspaceView = Backbone.View.extend({

        events: {},

        render:function() {
            var self = this;

            this.$versionedFiles = this.$("#versionedFiles");
            this.$unVersionedFiles = this.$("#unVersionedFiles");

            this.$versionedFiles.empty();
            this.$unVersionedFiles.empty();

            Commander.getPartMasters(function(pPartMarters) {
                var partMasters = JSON.parse(pPartMarters);

                _.each(partMasters, function (partMaster) {
                    if(!partMaster.isCheckedOut) {
                        var remoteVersionedFileModel = new RemoteVersionedFileModel({partNumber: partMaster.partNumber, name : partMaster.cadFileName, version : partMaster.version, status: partMaster});
                        var remoteVersionedFileView =  new RemoteVersionedFileView({model: remoteVersionedFileModel}).render();
                        self.$versionedFiles.append(remoteVersionedFileView.$el);
                    }
                });
            });
        }
    });

    return WorkspaceView;
});