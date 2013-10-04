define(["text!templates/menu.html","i18n!localization/nls/global","dplm","storage"], function(template, i18n, Dplm, Storage) {

    var MenuView = Backbone.View.extend({

        template: Handlebars.compile(template),

        events: {
            "click .add-path":"addPath",
            "click .workspace-item":"toggleActives",
            "click .path-item":"toggleActives",
            "click i.remove-path":"removePath",
            "click .edit-conf":"editConf"
        },

        initialize:function(){
            // Re-render menu view when a local folder is added
            APP_GLOBAL.SIGNALS.on("path:created",this.render,this);
        },

        render:function() {
            var self = this ;
            Dplm.getWorkspaces({
                success:function(workspaces){
                    self.$el.html(self.template({configuration:Storage.getGlobalConf(), paths:Storage.getLocalPaths(),workspaces: workspaces, i18n:i18n}));
                },
                error:function(){
                    APP_GLOBAL.SIGNALS.trigger("configuration:error");
                }
            });
            return this;
        },

        onConfigurationError:function(){
            this.$el.html(this.template({error:true,i18n:i18n}));
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
            require(["views/add_path"],function(AddPathView){
                var addPathView = new AddPathView();
                $("body").append(addPathView.render().el);
                addPathView.openModal();
            });
        },

        toggleActives:function(e){
            this.$(".active").removeClass("active");
            $(e.target).addClass("active");
        },

        editConf:function(){
            require(["views/configuration"],function(ConfigView){
                var configView = new ConfigView();
                $("body").append(configView.render().el);
                configView.openModal();
            });
        }


    });

    return MenuView;

});