/*global define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/models/organization',
    'common-objects/views/alert',
    'text!templates/organization-edit.html'
], function (Backbone, Mustache, Organization, AlertView, template) {
    'use strict';

    var OrganizationEdit = Backbone.View.extend({

        events: {
            'click .delete-organization':'deleteOrganization',
            'submit #organization_update_form':'onSubmit'
        },

        initialize: function () {
            this.render();
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                organization:App.config.organization,
                i18n:App.config.i18n
            }));
            this.$notifications = this.$('.notifications');
            return this;
        },

        deleteOrganization:function(){
            var _this = this;
            bootbox.confirm(
                '<h4>'+App.config.i18n.DELETE_ORGANIZATION_QUESTION+'</h4>'+
                '<p><i class="fa fa-warning"></i> '+App.config.i18n.DELETE_ORGANIZATION_TEXT+'</p>',
                App.config.i18n.CANCEL,
                App.config.i18n.DELETE,
                function(result){
                if(result){
                    Organization.deleteOrganization()
                        .then(_this.onDeleteOrganizationSuccess.bind(_this),_this.onError.bind(_this))
                        .then(function() {
                            App.config.organization = {};
                        });
                }
            });
        },

        onSubmit: function(e) {
            var name = this.$('#organization-name').val();
            var description = this.$('#description').val();

            Organization.updateOrganization({
                name:App.config.organization.name,
                description:description,
                owner_login:App.config.login
            }).then(this.onUpdateSucceed.bind(this), this.onError.bind(this));

            e.preventDefault();
            return false;
        },

        onUpdateSucceed:function(organization) {
            App.config.organization = organization;
            this.$notifications.append(new AlertView({
                type: 'success',
                message: App.config.i18n.ORGANIZATION_UPDATED
            }).render().$el);
        },

        onError:function(error){
            this.$notifications.append(new AlertView({
                type: 'error',
                message: error.responseText
            }).render().$el);
        },

        onDeleteOrganizationSuccess:function(){
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                deleting: true
            }));
        }

    });

    return OrganizationEdit;
});
