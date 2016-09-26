/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/app.html',
    'views/login-form',
    'views/recovery-form',
    'views/recover-form',
    'views/account-creation-form',
    'views/login-scene'
], function (Backbone, Mustache, template, LoginFormView, RecoveryFormView, RecoverFormView, AccountCreationFormView, LoginSceneView ) {
	'use strict';

    var AppView = Backbone.View.extend({

        el: '#content',

        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                contextPath:App.config.contextPath
            })).show();

            this.sceneView = new LoginSceneView({el:this.$('#demo-scene')[0]});
            this.$FormContainer = this.$('#form_container');
            this.showLoginForm();
            return this;
        },

        showRecoveryForm:function(){
            this.$FormContainer.html(new RecoveryFormView().render().$el);
            this.$FormContainer.attr('class','put-right');
            this.sceneView.handleResize();
        },

        showRecoverForm:function(uuid){
            this.$FormContainer.html(new RecoverFormView().render(uuid).$el);
            this.$FormContainer.attr('class','put-right');
            this.sceneView.handleResize();
        },

        showLoginForm:function(){
            this.$FormContainer.html(new LoginFormView().render().$el);
            this.$FormContainer.attr('class','put-right');
            this.sceneView.handleResize();
        },

        showAccountCreationForm:function(){
            this.$FormContainer.html(new AccountCreationFormView().render().$el);
            this.$FormContainer.attr('class','put-above');
            this.sceneView.handleResize();
        }


    });

    return AppView;
});
