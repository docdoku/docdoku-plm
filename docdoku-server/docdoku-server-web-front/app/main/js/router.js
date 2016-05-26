/*global define,App*/
define([
    'backbone',
    'common-objects/common/singleton_decorator'
],
function (Backbone, singletonDecorator) {
    'use strict';
    var Router = Backbone.Router.extend({
        routes: {
            '':   'login',
            'create-account':   'createAccount',
            'recovery':   'recovery',
        },

        login:function(){
            App.appView.showLoginForm();
	    },

        createAccount:function(){
            App.appView.showAccountCreationForm();
        },
        recovery:function(){
            App.appView.showRecoveryForm();
        }

    });

    return singletonDecorator(Router);
});
