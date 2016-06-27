/*global define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/models/workspace',
    'text!templates/content.html'
], function (Backbone, Mustache, Workspace, template) {
	'use strict';
    var AppView = Backbone.View.extend({
        el: '#content',

        events: {},

        initialize: function () {
            this.model = new Workspace({id: App.config.workspaceId});
        },

        render: function () {
            this.$el.html(Mustache.render(template, {model: this.model, i18n: App.config.i18n}));
	        this.$content = this.$('#change-management-content');

            App.$changeManagementMenu = this.$('#change-management-menu');
            App.$changeManagementMenu.customResizable({
                containment: this.$el
            });

            this.$el.show();
            return this;
        }

    });

    return AppView;
});
