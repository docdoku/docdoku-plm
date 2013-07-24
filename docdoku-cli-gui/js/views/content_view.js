define(["text!templates/content.html", "views/nav_view"], function(template, NavView) {

    var MainView =  Backbone.View.extend({

        template: Handlebars.compile(template),

        render:function() {
            this.$el.html(this.template({}));
            this.navView = new NavView().render();
            this.bindDomElements();
            return this;
        },

        bindDomElements:function(){

        },

        breadcrumbPath:function(path){
            this.navView.path(path);
        },

        breadcrumbWorkspace:function(workspace){
            this.navView.workspace(workspace);
        }

    });

    return MainView;
});