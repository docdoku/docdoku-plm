define(["text!templates/nav.html", "views/configuration","dplm", "i18n!localization/nls/global", "shell"], function(template, ConfigView, Dplm, i18n, Shell) {
    var NavView = Backbone.View.extend({

        el: 'div#nav',

        template: Handlebars.compile(template),

        events: {
            "click #home > a": "home",
            "click .open-path": "openPath"
        },

        render:function() {
            this.$el.html(this.template({}));
            this.bindDomElements();
            return this;
        },

        bindDomElements:function(){
            this.$breadcrumb = this.$("#breadcrumb");
        },

        path:function(path){
            this.currentPath = path;
            var lastSlash = path.lastIndexOf(OS_SLASH);
            var shortName = path.substr(lastSlash+1,path.length);
            this.$breadcrumb.html("<a class='open-path'>"+i18n.LOCAL_PATHS + " > " +shortName+"</a>");
        },

        openPath:function(){
            Shell.openPath(this.currentPath);
        },

        workspace:function(workspace){
            this.$breadcrumb.html("<a class='workspace'>"+ i18n.WORKSPACE + " > " +workspace+"</a>");
        },

        home:function(){
            APP_GLOBAL.ROUTER.navigate('home', { trigger: true });
        },

        onConfigurationError:function(){
            this.$el.html(this.template({error:true,i18n:i18n}));
        }

    });

    return NavView;
});