/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/admin-accounts.html',
    'common-objects/models/admin'
], function (Backbone, Mustache, template, Admin) {
    'use strict';

    var AdminAccountsView = Backbone.View.extend({

        events: {
            'change .toggle-checkboxes':'toggleCheckboxes',
            'click .toggle-checkbox':'toggleCheckbox',
            'change .toggle-checkbox':'toggleCheckboxChange',
            'click .enable-selected-accounts':'enableSelectedAccounts',
            'click .disable-selected-accounts':'disableSelectedAccounts'
        },

        initialize: function () {
        },

        render: function () {
            var _this = this;

            Admin.getAccounts().then(function(accounts){
                _this.$el.html(Mustache.render(template, {
                    i18n: App.config.i18n,
                    accounts:accounts
                }));
                _this.bindDOMElements();
                _this.toggleButtons();
                _this.disableSelf();
            });

            return this;
        },

        bindDOMElements: function () {
            this.$notifications = this.$('.notifications');
            this.$actionButtons = this.$('.accounts-actions');
            this.$updatableAccounts =  this.$('tbody > tr:not([data-login="'+App.config.login+'"])');
        },

        toggleButtons:function(){
            var hasAccounts = this.$updatableAccounts.find('td:nth-child(1) > input[type="checkbox"]:checked').size() > 0;
            this.$actionButtons.toggle(hasAccounts);
        },

        disableSelf:function(){
            this.$('tbody > tr[data-login="'+App.config.login+'"] > td:nth-child(1) > input[type="checkbox"]').prop('disabled', true);
        },

        toggleCheckboxChange:function(){

        },

        toggleCheckbox:function(){
            this.toggleButtons();
        },

        toggleCheckboxes:function(e){
            var $table = $(e.target).parents('table');
            $table.find('tbody > tr:not([data-login="'+App.config.login+'"]) > td:nth-child(1) > input[type="checkbox"]').prop('checked', e.target.checked).trigger('change');
            this.toggleButtons();
        },

        enableSelectedAccounts:function(){
            this.doBulkEnable(true);
        },

        disableSelectedAccounts:function(){
            this.doBulkEnable(false);
        },

        doBulkEnable:function(enabled){
            var accounts = this.$('tbody > tr:not([data-login="'+App.config.login+'"]) > td:nth-child(1) > input[type="checkbox"]:checked');
            var ajaxes = [];
            accounts.each(function(index,checkbox){
                var $tr = $(checkbox).parents('tr');
                var login = $tr.data('login');
                ajaxes.push(Admin.enableAccount(login,enabled).then(function(){
                    $tr.toggleClass('account-enabled',enabled);
                }));
            });
            $.when.apply($, ajaxes);
        }

    });

    return AdminAccountsView;
});
