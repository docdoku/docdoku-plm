/*global define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/models/organization',
    'common-objects/views/alert',
    'text!templates/organization-create.html'
], function (Backbone, Mustache, Organization, AlertView, template) {
    'use strict';

    var OrganizationCreate = Backbone.View.extend({

        events: {
            'submit #organization_creation_form':'onSubmit'
        },

        initialize: function () {
            this.render();
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                organizationName: App.config.organization.name,
                i18n: App.config.i18n
            }));
            this.$notifications = this.$('.notifications');
            return this;
        },

        onSubmit: function(e) {
            var name = this.$('#organization-name').val();
            var description = this.$('#description').val();

            Organization.createOrganization({
                name:name,
                description:description
            }).then(this.onSuccess.bind(this), this.onError.bind(this));

            e.preventDefault();
            return false;
        },

        onSuccess:function(organization) {
            App.config.organization = organization;
            App.config.organizationName = organization.name;
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                organizationCreated: true
            }));
        },

        onError:function(error){
            this.$notifications.append(new AlertView({
                type: 'error',
                message: error.responseText
            }).render().$el);
        }

    });

    return OrganizationCreate;
});
