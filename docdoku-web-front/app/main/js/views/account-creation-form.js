/*global App*/
define([
    'backbone',
    'mustache',
    'text!templates/account-creation-form.html',
    'common-objects/views/alert',
    'common-objects/models/timezone',
    'common-objects/models/language'
], function (Backbone, Mustache, template, AlertView, TimeZone, Language) {
    'use strict';

    var AccountCreationFormView = Backbone.View.extend({

        tagName:'form',
        id:'account_creation_form',
        events:{
            'submit':'onAccountCreationFormSubmit'
        },

        render: function () {


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
                    _this.$confirmPassword = _this.$('#account_creation_form-confirmPassword');
                    _this.$password = _this.$('#account_creation_form-password');
                    _this.$('#account_creation_form-language option').each(function(){
                        $(this).text(App.config.i18n.LANGUAGES[$(this).val()]);
                    });
                    _this.$('#account_creation_form-timeZone option').each(function(){
                        // Fixed type error. Assume CET is in list.
                        // TODO : detect browser timezone for auto selection
                        $(this).attr('selected', $(this).val() === 'CET');
                    });
                });

            return this;
        },

        onAccountCreationFormSubmit:function(e){

            this.$notifications.empty();

            if(this.$password.val() !== this.$confirmPassword.val()){
                this.$notifications.append(new AlertView({
                    type: 'error',
                    message: App.config.i18n.PASSWORD_NOT_CONFIRMED
                }).render().$el);
                e.preventDefault();
                return false;
            }

            $.ajax({
                type: 'POST',
                url: App.config.contextPath + '/api/accounts/create',
                data: JSON.stringify({
                    login:this.$('#account_creation_form-login').val(),
                    name:this.$('#account_creation_form-name').val(),
                    email:this.$('#account_creation_form-email').val(),
                    language:this.$('#account_creation_form-language').val(),
                    timeZone:this.$('#account_creation_form-timeZone').val(),
                    newPassword:this.$('#account_creation_form-password').val()
                }),
                contentType: 'application/json; charset=utf-8'
            }).then(this.onAccountCreated.bind(this), this.onAccountCreationFailed.bind(this));
            e.preventDefault();
            return false;
        },

        onAccountCreated : function(account){
            if(account.enabled){
                window.location.href = App.config.contextPath + '/workspace-management/?accountCreated=true';
            } else {
                this.$notifications.append(new AlertView({
                    type: 'success',
                    message: App.config.i18n.ACCOUNT_NOT_ENABLED_YET
                }).render().$el);
            }
        },

        onAccountCreationFailed:function(err){
            this.$notifications.append(new AlertView({
                type: 'error',
                message: err.responseText
            }).render().$el);
        }
    });

    return AccountCreationFormView;
});
