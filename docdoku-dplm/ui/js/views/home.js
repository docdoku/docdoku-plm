define(["text!templates/home.html","storage","dplm"], function(template, Storage,Dplm) {

    var MainView =  Backbone.View.extend({

        el:"div#subContent",

        template: Handlebars.compile(template),

        events:{
            "click .add-path":"addPath",
            "click .item" : "toggleActives",
            "click .icon-cogs":"configuration"
        },

        render:function() {
            var self = this;
            var recentlyUsedPaths = Storage.getRecentlyUsedPaths();
            var recentlyUsedWorkspaces = Storage.getRecentlyUsedWorkspaces();
            Dplm.getWorkspaces({
                success:function(workspaces){
                    recentlyUsedWorkspaces = _.intersection(recentlyUsedWorkspaces,workspaces);
                    self.$el.html(self.template({user:APP_GLOBAL.GLOBAL_CONF.user,recentlyUsedPaths:recentlyUsedPaths.reverse(),recentlyUsedWorkspaces:recentlyUsedWorkspaces.reverse()}));
                },
                error:function(){
                    self.$el.html(self.template({user:APP_GLOBAL.GLOBAL_CONF.user,recentlyUsedPaths:recentlyUsedPaths.reverse(),recentlyUsedWorkspaces:recentlyUsedWorkspaces.reverse()}));
                }
            });
            this.bindDomElements();
            return this;
        },

        bindDomElements:function(){
        },

        addPath:function(){
            require(["views/add_path"],function(AddPathView){
                var addPathView = new AddPathView();
                $("body").append(addPathView.render().el);
                addPathView.openModal();
            });
        },

        toggleActives:function(e){
            var href = $(e.currentTarget).attr("href");
            $("#menu .active").removeClass("active");
            $("#menu").find("a[href='"+href+"']").addClass("active");
        },

        configuration:function(){
            require(["views/configuration"],function(ConfigView){
                var configView = new ConfigView();
                $("body").append(configView.render().el);
                configView.openModal();
            });
        },

        onConfigurationError:function(){
            this.$el.empty();
        }

    });

    return MainView;
});