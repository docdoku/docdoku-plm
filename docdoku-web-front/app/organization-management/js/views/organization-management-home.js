/*global define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/models/organization',
    'common-objects/views/alert',
    'text!templates/organization-management-home.html'
], function (Backbone, Mustache, Organization, AlertView, template) {
    'use strict';

    var OrganizationManagementHome = Backbone.View.extend({

        events: {
            'submit #organization_creation_form':'onSubmit'
        },

        initialize: function () {
            this.render();
        },

        render: function () {
            var _this = this;

            this.$el.html(Mustache.render(template, {
                organization:App.config.organization,
                organizationName:App.config.organization.name,
                i18n: App.config.i18n
            }));
            this.$notifications = this.$('.notifications');

            if(!_.isEmpty(App.config.organization)) {
                Organization.getMembers().then(function(members) {
                    _this.$('.members-count').text(members.length);
                });
            }

            _this.$('.subscriptions-count').text('0');

            return this;
        },

        onError:function(error){
            this.$notifications.append(new AlertView({
                type: 'error',
                message: error.responseText
            }).render().$el);
        }

    });

    return OrganizationManagementHome;
});
