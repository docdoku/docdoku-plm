/*global define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/models/workspace',
	'common-objects/collections/baselines',
    'text!templates/content.html'
], function (Backbone, Mustache, Workspace, Baselines, template) {
	'use strict';
    var AppView = Backbone.View.extend({

        el: '#content',

        initialize: function () {
            this.model = new Workspace({id: App.config.workspaceId});
        },

        render: function () {

            this.$el.html(Mustache.render(template, {model: this.model, i18n: App.config.i18n}));
            App.$documentManagementMenu = this.$('#document-management-menu');
            App.$documentManagementContent = this.$('#document-management-content');

            this.bindDomElements();

            App.$documentManagementMenu.customResizable({
                containment: this.$el
            });
            this.$el.show();
            return this;
        },

        bindDomElements:function(){
            this.$linksNav = this.$('.nav-header.links-nav');
        }
    });

    return AppView;
});
