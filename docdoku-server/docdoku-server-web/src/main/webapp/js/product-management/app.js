define([
    "router",
    "common-objects/models/workspace",
    "modules/navbar-module/views/navbar_view",
    "text!templates/content.html",
    "i18n!localization/nls/product-management-strings"
], function (Router, Workspace, NavBarView, template, i18n) {

    var AppView = Backbone.View.extend({

        el: $("#content"),

        events: {
        },

        template:Mustache.compile(template),

        initialize: function() {
            this.workspace = new Workspace({
                id: APP_CONFIG.workspaceId
            });
        },

        render:function(){
            this.$el.html(this.template({model:this.workspace,i18n:i18n}));
            this.bindDomElements();
            this.menuResizable();
            return this ;
        },

        menuResizable:function(){
            this.$productManagementMenu.resizable({
                containment: this.$el,
                handles: 'e',
                autoHide: true,
                stop: function(e, ui) {
                    var parent = ui.element.parent();
                    ui.element.css({
                        width: ui.element.width()/parent.width()*100+"%",
                        height: "100%"
                    });
                }
            });
        },

        bindDomElements:function(){
            this.$productManagementMenu = this.$("#product-management-menu");
        }

    });

    new AppView().render();

    Router.getInstance();
    Backbone.history.start();
});