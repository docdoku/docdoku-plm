/*global _,require,window*/

var App = {};

require.config({

    baseUrl: 'js',

    shim: {
        jqueryUI: { deps: ['jquery'], exports: 'jQuery' },
        bootstrap: { deps: ['jquery', 'jqueryUI'], exports: 'jQuery' },
        backbone: { deps: ['underscore', 'jquery'], exports: 'Backbone'}

    },

    paths: {
        jquery: '../../bower_components/jquery/jquery',
        jqueryUI: '../../bower_components/jqueryui/ui/jquery-ui',
        backbone: '../../bower_components/backbone/backbone',
        underscore: '../../bower_components/underscore/underscore',
        mustache: '../../bower_components/mustache/mustache',
        text: '../../bower_components/requirejs-text/text',
        i18n: '../../bower_components/requirejs-i18n/i18n',
        bootstrap: '../../bower_components/bootstrap/docs/assets/js/bootstrap',
        'common-objects': '../../js/common-objects',
        localization: '../../js/localization',
        pluginDetect:'../../js/lib/plugin-detect'
    },

    deps: [
        'jquery',
        'underscore',
        'bootstrap',
        'jqueryUI',
        'pluginDetect'
    ],
    config: {
        i18n: {
            locale: (function(){
                'use strict';
                try{
                    return window.localStorage.locale || 'en';
                }catch(ex){
                    return 'en';
                }
            })()
        }
    }
});

require(['common-objects/contextResolver','i18n!localization/nls/common','i18n!localization/nls/index'],
    function (ContextResolver, commonStrings, indexStrings) {
        'use strict';

        App.config.i18n = _.extend(commonStrings, indexStrings);

        ContextResolver.resolveServerProperties()
            .then(function buildView(){
                require(['backbone','app','router','common-objects/views/header'],function(Backbone, AppView, Router, HeaderView){
                    App.appView = new AppView();
                    App.headerView = new HeaderView();
                    App.appView.render();
                    App.headerView.render();
                    App.router = Router.getInstance();
                    Backbone.history.start();
                });
            });
    });

