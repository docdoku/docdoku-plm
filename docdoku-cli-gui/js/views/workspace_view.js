define([
    "text!templates/workspace.html",
    "models/remote_versioned_file_model",
    "views/remote_versioned_file_view",
    "commander"],
    function(template, RemoteVersionedFileModel, RemoteVersionedFileView, Commander) {
    var WorkspaceView = Backbone.View.extend({

        el:"div#subContent",

        template: Handlebars.compile(template),

        events: {},

        render:function() {

            this.$el.html(this.template({}));

            this.$workspace = $("#workspace");

            var self = this;

            Commander.getPartMasters(function(pPartMarters) {
                var partMasters = JSON.parse(pPartMarters);

                _.each(partMasters, function (partMaster) {
                    if(!partMaster.isCheckedOut) {
                        var remoteVersionedFileModel = new RemoteVersionedFileModel({partNumber: partMaster.partNumber, name : partMaster.cadFileName, version : partMaster.version, status: partMaster});
                        var remoteVersionedFileView =  new RemoteVersionedFileView({model: remoteVersionedFileModel}).render();
                        self.$workspace.append(remoteVersionedFileView.$el);
                    }
                });
            });
        }
    });

    return WorkspaceView;
});