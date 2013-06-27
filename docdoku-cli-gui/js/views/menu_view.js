define(["text!templates/menu.html", "storage", "views/local_dir_view", "views/workspace_view"], function(template, Storage, LocalDirView, WorkspaceView) {
    var MenuView = Backbone.View.extend({

        template: Handlebars.compile(template),

        events: {
          "click #localFiles" : "onClickLocalFiles",
          "click #dplm" : "onClickDplm"
        },

        render:function() {
            this.$el.html(this.template({}));

            return this;
        },

        onClickLocalFiles:function() {
            var localDirView = new LocalDirView({el: "#localRepo"}).render();
        },

        onClickDplm:function() {
            var workspaceView = new WorkspaceView({el : "#localRepo"}).render();
        }
    });

    return MenuView;
});