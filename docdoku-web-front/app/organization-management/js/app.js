/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/content.html',
    'common-objects/models/organization',
    'views/organization-management-home',
    'views/organization-create',
    'views/organization-edit',
    'views/organization-members'
], function (Backbone, Mustache, template, Organization, OrganizationManagementHome, OrganizationCreate, OrganizationEdit, OrganizationMembers) {
    'use strict';

    var AppView = Backbone.View.extend({

        el: '#content',

        events: {
            'click .organization-management': 'navigateOrganizationManagement'
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n
            })).show();
            this.$notifications = this.$('.notifications');
            return this;
        },

        navigateOrganizationManagement: function () {
            window.location.hash = '#/';
        },

        organizationManagementHome: function () {
            var view = new OrganizationManagementHome();
            view.render();
            this.$('#organization-management-content').html(view.render().el);
        },

        organizationCreate: function () {
            var view = new OrganizationCreate();
            view.render();
            this.$('#organization-management-content').html(view.render().el);
        },

        organizationEdit: function () {
            var view = new OrganizationEdit();
            view.render();
            this.$('#organization-management-content').html(view.render().el);
        },

        organizationMembers: function () {
            var view = new OrganizationMembers();
            view.render();
            this.$('#organization-management-content').html(view.render().el);
        }

    });

    return AppView;
});
