define(["text!templates/nav.html", "views/configuration_view","commander", "i18n!localization/nls/global"], function(template, ConfigView, Commander,i18n) {
    var NavView = Backbone.View.extend({

        el: 'div#nav',

        template: Handlebars.compile(template),

        events: {
            "click .icon-cog" : "openConfigView",
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
            var shortName = "";
            var lastSlash = path.lastIndexOf(OS_SLASH);
            var shortName = path.substr(lastSlash+1,path.length);
            this.$breadcrumb.html("<a class='open-path'>"+i18n.LOCAL_PATHS + " > " +shortName+"</a>");
        },

        openPath:function(){
            Commander.openPath(this.currentPath);
        },

        workspace:function(workspace){
            this.$breadcrumb.html("<a class='workspace'>"+ i18n.WORKSPACE + " > " +workspace+"</a>");
        },

        openConfigView:function() {
            var configView = new ConfigView();
            $("body").append(configView.render().el);
            configView.openModal();
        }
    });

    return NavView;
});