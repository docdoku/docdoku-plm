define([
    'backbone',
    'mustache',
    'text!templates/recovery-form.html',
    'common-objects/views/alert'
], function (Backbone, Mustache, template, AlertView) {
    'use strict';

    var RecoveryFormView = Backbone.View.extend({

        tagName:'form',
        id:'recovery_form',
        events:{
            'submit':'onRecoveryFormSubmit'
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                contextPath:App.config.contextPath
            }));
            this.$notifications = this.$('.notifications');
            return this;
        },


        onRecoveryFormSubmit:function(e){
            this.$notifications.empty();
            var login = this.$('#recovery_form-login').val();
            $.ajax({
                type: 'POST',
                url: App.config.contextPath + '/api/auth/recovery',
                data: JSON.stringify({
                    login:login
                }),
                contentType: 'application/json; charset=utf-8'
            }).then(this.onRecoverySent.bind(this), this.onError.bind(this));
            e.preventDefault();
            return false;
        },

        onRecoverySent:function(){
            this.$notifications.append(new AlertView({
                type: 'success',
                message: App.config.i18n.RECOVERY_REQUEST_SENT
            }).render().$el);
        },

        onError:function(err){
            this.$notifications.append(new AlertView({
                    type: 'error',
                    message: err.responseText
            }).render().$el);
        }

    });

    return RecoveryFormView;
});
