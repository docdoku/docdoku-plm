/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/edit-account.html',
    'common-objects/models/timezone',
    'common-objects/models/language',
    'common-objects/models/user',
    'common-objects/views/alert'
], function (Backbone, Mustache, template, TimeZone, Language, User, AlertView) {
    'use strict';

    var EditAccountView = Backbone.View.extend({
        events:{
            'click .toggle-password-update':'togglePasswordUpdate',
            'submit #account_edition_form':'onSubmitForm',
            'click .logout':'logout'
        },

        render:function(){

            this.enablePasswordUpdate = false;
            var _this = this;

            TimeZone.getTimeZones()
                .then(function(timeZones){
                _this.timeZones = timeZones;
            })
            .then(Language.getLanguages)
            .then(function(languages){
                _this.languages = languages;
            })
            .then(function(){

                _this.$el.html(Mustache.render(template, {
                    i18n: App.config.i18n,
                    account:App.config.account,
                    timeZones: _this.timeZones,
                    languages: _this.languages
                }));
                _this.$notifications = _this.$('.notifications');
                _this.$('#account-language option').each(function(){
                    $(this).attr('selected', $(this).val() === App.config.account.language);
                    $(this).text(App.config.i18n.LANGUAGES[$(this).val()]);
                });
                _this.$('#account-timezone option').each(function(){
                    $(this).attr('selected', $(this).val() === App.config.account.timeZone);
                });

            });

            return this;
        },

        onSubmitForm:function(e){

            var account = {
                name:this.$('#account-name').val().trim(),
                email:this.$('#account-email').val().trim(),
                language:this.$('#account-language').val(),
                timeZone:this.$('#account-timezone').val()
            };

            if(this.enablePasswordUpdate){
                var newPassword = this.$('#account-password').val().trim();
                var confirmedPassword = this.$('#account-confirm-password').val().trim();
                if(newPassword === confirmedPassword){
                    account.newPassword = newPassword;
                }else{
                    this.$notifications.append(new AlertView({
                        type: 'error',
                        message: App.config.i18n.PASSWORD_NOT_CONFIRMED
                    }).render().$el);
                    e.preventDefault();
                    return false;
                }
            }

            User.updateAccount(account)
                .then(this.onUpdateSuccess.bind(this),this.onError.bind(this));

            e.preventDefault();
            return false;
        },

        onError:function(error){
            this.$notifications.append(new AlertView({
                type: 'error',
                message: error.responseText
            }).render().$el);
        },

        onUpdateSuccess:function(account){
            var needReload = window.localStorage.locale !== account.language;
            if(window.localStorage.locale !== account.language){
                window.localStorage.locale = 'unset';
            }

            this.$notifications.append(new AlertView({
                type: 'success',
                title: App.config.i18n.ACCOUNT_UPDATED,
                message: needReload ? App.config.i18n.NEED_PAGE_RELOAD_CHANGED_LANG : ''
            }).render().$el);
        },

        togglePasswordUpdate:function(){
            this.enablePasswordUpdate = !this.enablePasswordUpdate;
            this.$('.password-update').toggle(this.enablePasswordUpdate);
            this.$('#account-password').attr('required',this.enablePasswordUpdate);
            this.$('#account-confirm-password').attr('required',this.enablePasswordUpdate);
        },

        logout:function(){
            delete localStorage.jwt;
            $.get(App.config.contextPath + '/api/auth/logout').complete(function () {
                window.location.href = App.config.contextPath + '/?logout=true';
            });
        }
    });

    return EditAccountView;
});
