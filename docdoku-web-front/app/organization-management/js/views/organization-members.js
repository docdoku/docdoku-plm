/*global define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/models/organization',
    'common-objects/views/alert',
    'text!templates/organization-members.html'
], function (Backbone, Mustache, Organization, AlertView, template) {
    'use strict';

    var OrganizationMembers = Backbone.View.extend({

        events: {
            'click .add-user':'addUserForm',
            'click #organization-add-user-form .cancel':'cancelAddUserForm',
            'submit #organization-add-user-form':'onAddUserFormSubmit',
            'change .toggle-checkboxes':'toggleCheckboxes',
            'click .toggle-checkbox':'toggleCheckbox',
            'change .toggle-checkbox':'toggleCheckboxChange',
            'click .move-member-up':'moveMemberUp',
            'click .move-member-down':'moveMemberDown',
            'click .delete-users':'deleteUsers'
        },

        initialize: function () {
            this.render();
        },

        render: function () {
            var _this = this;

            Organization.getMembers().then(function(members) {
                _this.members = members;
            }).then(function() {
                _this.$el.html(Mustache.render(template, {
                    i18n: App.config.i18n,
                    members:_this.members
                }));
                _this.bindDOMElements();
            });
            return this;
        },

        bindDOMElements: function() {
            this.$notifications = this.$('.notifications');
            this.$addUserForm = this.$('#organization-add-user-form');
            this.$addUserFormButton = this.$('.add-user');
        },

        addUserForm:function(){
            this.$addUserForm.removeClass('hide');
            this.$addUserFormButton.hide();
        },

        cancelAddUserForm:function(){
            this.$addUserForm.addClass('hide');
            this.$addUserFormButton.show();
        },

        onAddUserFormSubmit:function(e){
            var login = this.$('#organization-add-user-form input[name="login"]').val().trim();
            if(login){
                Organization.addMember({login:login})
                    .then(this.render.bind(this), this.onError.bind(this));
            }
            e.preventDefault();
            return false;
        },

        moveMemberUp:function(){
            var usersChecked = this.$('tbody > tr > td:nth-child(1) > input[type="checkbox"]:checked');
            if(usersChecked.length === 1) {
                var userLogin = usersChecked[0].dataset.login;
                Organization.moveMemberUp({login:userLogin})
                    .then(this.render.bind(this), this.onError.bind(this));
            }
        },

        moveMemberDown:function(){
            var usersChecked = this.$('tbody > tr > td:nth-child(1) > input[type="checkbox"]:checked');
            if(usersChecked.length === 1) {
                var userLogin = usersChecked[0].dataset.login;
                Organization.moveMemberDown({login:userLogin})
                    .then(this.render.bind(this), this.onError.bind(this));
            }
        },

        deleteUsers:function(){
            var userLogins=[];
            this.$('tbody > tr > td:nth-child(1) > input[type="checkbox"]:checked')
                .each(function(index,checkbox){
                    userLogins.push(checkbox.dataset.login);
                });

            var usersToDelete = _.without(_.uniq(userLogins),App.config.login);

            Organization.removeMembers(usersToDelete)
                .then(this.render.bind(this), this.onError.bind(this));
        },

        onError:function(error){
            this.$notifications.append(new AlertView({
                type: 'error',
                message: error.responseText
            }).render().$el);
        },

        toggleButtons:function(){
            var hasUsers = this.$('tbody > tr > td:nth-child(1) > input[type="checkbox"]:checked').size() > 0;
            var hasUser = this.$('tbody > tr > td:nth-child(1) > input[type="checkbox"]:checked').size() === 1;
            this.$('.show-if-user').toggle(hasUser);
            this.$('.show-if-users').toggle(hasUsers);
        },

        toggleCheckboxChange:function(){
        },

        toggleCheckbox:function(){
            this.toggleButtons();
        },

        toggleCheckboxes:function(e){
            var $table = $(e.target).parents('table');
            $table.find('tbody > tr > td:nth-child(1) > input[type="checkbox"]').prop('checked', e.target.checked).trigger('change');
            this.toggleButtons();
        }

    });

    return OrganizationMembers;
});
