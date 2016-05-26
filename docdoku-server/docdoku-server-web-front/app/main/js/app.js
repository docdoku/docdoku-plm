/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/app.html',
    'views/login-form',
    'views/recovery-form',
    'views/login-scene'
], function (Backbone, Mustache, template, LoginFormView, RecoveryFormView, LoginSceneView ) {
	'use strict';

    var AppView = Backbone.View.extend({

        el: '#content',

        events:{
            'click .recovery-link':'showRecoveryForm',
            'click .login-form-link':'showLoginForm'
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                contextPath:App.config.contextPath
            })).show();

            this.loginFormView = new LoginFormView().render();
            this.recoveryFormView = new RecoveryFormView().render();
            this.loginSceneView = new LoginSceneView({el:this.$('#demo-scene')[0]});

            this.$loginFormContainer = this.$('#login_form_container');
            this.$loginFormContainer.append(this.loginFormView.$el);
            this.$loginFormContainer.append(this.recoveryFormView.$el);

            return this;
        },

        showRecoveryForm:function(){
            this.recoveryFormView.$el.show();
            this.loginFormView.$el.hide();
        },

        showLoginForm:function(){
            this.loginFormView.$el.show();
            this.recoveryFormView.$el.hide();
        }


    });

    return AppView;
});
