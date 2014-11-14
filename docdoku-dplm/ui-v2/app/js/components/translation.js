angular.module('dplm.services.translations',['pascalprecht.translate'])
.config(function($translateProvider) {

        $translateProvider.translations('en', {
            HELLO:'Hello'
        })
        .translations('fr', {
            HELLO:'Bonjour'
        });

        $translateProvider.preferredLanguage(localStorage.lang || 'en');

});