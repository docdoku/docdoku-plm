define(["text!templates/menu.html","i18n!localization/nls/global","commander","storage"], function(template, i18n, Commander, Storage) {

    var MenuView = Backbone.View.extend({

        template: Handlebars.compile(template),

        events: {
            "click .add-path":"addPath",
            "click .workspace-item":"toggleActives",
            "click .path-item":"toggleActives",
            "click i.remove-path":"removePath"
        },

        initialize:function(){
            APP_GLOBAL.SIGNALS.on("path:created",this.render,this);
        },

        render:function() {
            var self = this ;
            Commander.getWorkspaces({
                success:function(workspacesStr){
                    var workspaces = JSON.parse(workspacesStr);
                    self.$el.html(self.template({configuration:Storage.getGlobalConf(), paths:Storage.getLocalPaths(),workspaces: workspaces, i18n:i18n}));
                },
                error:function(){
                    self.$el.html(self.template({error:true,i18n:i18n}));
                }
            });
        },

        removePath:function(e){
            e.preventDefault();
            e.stopPropagation();
            if(confirm(i18n.REMOVE_PATH + " ?")){
                var path = $(e.target).data("path");
                Storage.removePath(path);
                this.render();
            }
        },

        addPath:function(){
            require(["views/add_path_view"],function(AddPathView){
                var addPathView = new AddPathView();
                $("body").append(addPathView.render().el);
                addPathView.openModal();
            });
        },

        toggleActives:function(e){
            this.$(".active").removeClass("active");
            $(e.target).addClass("active");
        }


    });

    return MenuView;

});