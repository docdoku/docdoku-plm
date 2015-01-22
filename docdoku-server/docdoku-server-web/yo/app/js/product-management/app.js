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

        template: Mustache.parse(template),

        initialize: function () {
            this.model = new Workspace({id: App.config.workspaceId});
        },

        render: function () {

            this.$el.html(Mustache.render(template, {model: this.model, i18n: App.config.i18n}));

            App.$productManagementMenu = this.$('#product-management-menu');
            App.$productManagementContent = this.$('#product-management-content');

            App.$productManagementMenu.customResizable({
                containment: this.$el
            });

            this.$el.show();
            return this;
        }

    });

    return AppView;
});
