/*global define*/
define([
    'backbone',
    "mustache",
    "common-objects/models/workspace",
    "text!templates/content.html"
], function (Backbone, Mustache, Workspace, template) {

    var AppView = Backbone.View.extend({

        el: $("#content"),

        events: {},

        initialize: function () {
            this.model = new Workspace({id: APP_CONFIG.workspaceId});
        },

        render: function () {

            this.$el.html(Mustache.render(template, {model: this.model, i18n: APP_CONFIG.i18n}));
            App.$documentManagementMenu = this.$("#document-management-menu");
            App.$documentManagementMenu.customResizable({
                containment: this.$el
            });
            this.$el.show();
            return this;
        }

    });

    return AppView;
});