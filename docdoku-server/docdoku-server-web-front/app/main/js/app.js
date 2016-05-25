/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/login.html',
    'views/login-scene'
], function (Backbone, Mustache, template, LoginSceneView) {
	'use strict';


    function getParameterByName(name) {
        var url = window.location.href;
        name = name.replace(/[\[\]]/g, "\\$&");
        var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
            results = regex.exec(url);
        if (!results) return null;
        if (!results[2]) return '';
        return decodeURIComponent(results[2].replace(/\+/g, " "));
    }

    var AppView = Backbone.View.extend({

        el: '#content',

        events:{
            'click .recovery-link':'showRecoveryForm',
            'click .login-form-link':'showLoginForm',
            'submit #login_form':'onLoginFormSubmit',
            'submit #recovery_form':'onRecoveryFormSubmit',
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                contextPath:App.config.contextPath
            })).show();
            this.$loginForm = this.$('#login_form');
            this.$recoveryForm = this.$('#recovery_form');
            new LoginSceneView({el:this.$('#demo-scene')[0]});
            return this;
        },

        showRecoveryForm:function(){
            this.$recoveryForm.show()
            this.$loginForm.hide();
        },

        showLoginForm:function(){
            this.$loginForm.show();
            this.$recoveryForm.hide();
        },

        onLoginFormSubmit:function(e){
            delete localStorage.jwt;
            $.ajax({
                type: 'POST',
                url: App.config.contextPath + '/api/auth/login',
                data: JSON.stringify({
                    login:this.$('#login_form-login').val(),
                    password:this.$('#login_form-password').val()
                }),
                contentType: 'application/json; charset=utf-8'
            }).then(function(account, status, xhr ){
                var jwt = xhr.getResponseHeader('jwt');
                localStorage.jwt = jwt;
                var originURL = getParameterByName('originURL');
                window.location.href = originURL ? decodeURIComponent(originURL):App.config.contextPath + '/workspace-management/';
            }, this.onLoginFailed.bind(this));
            e.preventDefault();
            return false;
        },

        onRecoveryFormSubmit:function(e){
            var login = this.$('#recovery_form-login').val();
            $.ajax({
                type: 'POST',
                url: App.config.contextPath + '/api/auth/recovery',
                data: JSON.stringify({
                    login:login
                }),
                contentType: 'application/json; charset=utf-8'
            }).then(function(account, status, xhr ){
                window.location.href = App.config.contextPath + '/?recoverySent='+login;
            }, this.onLoginFailed.bind(this));
            e.preventDefault();
            return false;
        },

        onLoginFailed:function(err){

        }

    });

    return AppView;
});
