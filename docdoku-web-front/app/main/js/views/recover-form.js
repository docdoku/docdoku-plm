/*global App*/
define([
    'backbone',
    'mustache',
    'text!templates/recover-form.html',
    'common-objects/views/alert'
], function (Backbone, Mustache, template, AlertView) {
    'use strict';

    var RecoverFormView = Backbone.View.extend({

        tagName:'form',
        id:'recovery_form',
        events:{
            'submit':'onRecoverFormSubmit'
        },

        render: function (uuid) {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                contextPath:App.config.contextPath
            }));
            this.uuid = uuid;
            this.$notifications = this.$('.notifications');
            this.$password = this.$('#recover_form-password');
            this.$confirmPassword = this.$('#recover_form-confirmPassword');

            return this;
        },


        onRecoverFormSubmit:function(e){
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
                url: App.config.contextPath + '/api/auth/recover',
                data: JSON.stringify({
                    uuid:this.uuid,
                    newPassword:this.$password.val()
                }),
                contentType: 'application/json; charset=utf-8'
            }).then(this.onRecoverSent.bind(this), this.onError.bind(this));
            e.preventDefault();

            return false;
        },

        onRecoverSent:function(){
            this.$('.form-zone').after('<p>'+App.config.i18n.RECOVER_OK+'</p>');
            this.$('.form-zone').hide();
        },

        onError:function(err){
            this.$notifications.append(new AlertView({
                type: 'error',
                message: err.responseText
            }).render().$el);

        }

    });

    return RecoverFormView;
});
