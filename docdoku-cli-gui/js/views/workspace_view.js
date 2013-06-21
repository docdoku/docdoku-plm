define([
    "models/versioned_file_model",
    "views/versioned_file_view",
    "commander"],
    function(VersionedFileModel, VersionedFileView, Commander) {
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
                    var versionedFileModel = new VersionedFileModel({partNumber: partMaster.partNumber, name : partMaster.cadFileName, version : partMaster.version, status: partMaster});
                    var versionedFileView =  new VersionedFileView({model: versionedFileModel}).render();
                    self.$versionedFiles.append(versionedFileView.$el);
                });
            });
        }
    });

    return WorkspaceView;
});