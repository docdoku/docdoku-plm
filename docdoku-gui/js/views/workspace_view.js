define([
    "text!templates/workspace.html",
    "models/remote_versioned_file_model",
    "views/remote_versioned_file_view",
    "views/loader_view",
    "commander"
    ],
    function(template, RemoteVersionedFileModel, RemoteVersionedFileView, Loader, Commander) {
    var WorkspaceView = Backbone.View.extend({

        el:"div#subContent",

        template: Handlebars.compile(template),

        events: {},

        setWorkspace:function(workspace){
            this.workspace = workspace;
            return this;
        },

        render:function() {

            this.$el.html(this.template({workspace:this.workspace}));
            this.$workspace = $("#workspace");
            this.$workspace.html(new Loader);

            var self = this;

            Commander.getPartMasters(this.workspace,function(pPartMasters) {

                self.$workspace.empty();

                var partMasters = JSON.parse(pPartMasters);

                _.each(partMasters, function (partMaster) {
                    var remoteVersionedFileModel = new RemoteVersionedFileModel({partNumber: partMaster.partNumber, name : partMaster.cadFileName, version : partMaster.version, status: partMaster});
                    var remoteVersionedFileView =  new RemoteVersionedFileView({model: remoteVersionedFileModel}).render();
                    self.$workspace.append(remoteVersionedFileView.$el);
                });
            });
        }
    });

    return WorkspaceView;
});