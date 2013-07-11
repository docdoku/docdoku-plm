define(["text!templates/menu.html",
        "i18n!localization/nls/global",
        "storage",
        "views/local_dir_view",
        "views/workspace_view"],
    function(template, i18n, Storage, LocalDirView, WorkspaceView) {
    var MenuView = Backbone.View.extend({

        template: Handlebars.compile(template),

        events: {
          "click #localFiles" : "onClickLocalFiles",
          "click #dplm" : "onClickDplm"
        },

        render:function() {
            this.$el.html(this.template({workspace: Storage.getWorkspace(), i18n:i18n}));

            return this;
        },

        onClickLocalFiles:function() {
            if(this.localDirView) {
                this.localDirView.remove();
            }
            this.localDirView = new LocalDirView().render();
        },

        onClickDplm:function() {
            if(this.workspaceView) {
                this.workspaceView.remove();
            }
            this.workspaceView = new WorkspaceView().render();
        }
    });

    return MenuView;
});