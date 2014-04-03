define([
    "common-objects/models/workspace",
    "text!templates/content.html",
    "i18n!localization/nls/change-management-strings"
], function (Workspace, template, i18n) {
    var AppView = Backbone.View.extend({

        el: $("#content"),

        events: {},

        template:Mustache.compile(template),

        initialize: function() {
            this.model = new Workspace({id: APP_CONFIG.workspaceId});
        },

        render:function(){
            this.$el.html(
                this.template({model: this.model,
                               i18n:i18n}));
            this.bindDomElements();
            this.$changeManagementMenu.customResizable({
                containment: this.$el
            });
            return this;
        },

        bindDomElements:function(){
            this.$changeManagementMenu = this.$("#change-management-menu");
        }

    });

    return AppView;
});