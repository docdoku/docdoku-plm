/*global _,require,window*/

var App = {
    debug:false,
	config:{
		login: '',
		groups: [],
		contextPath: '',
		locale: window.localStorage.getItem('locale') || 'en'
	}
};

App.log=function(message){
    'use strict';
    if(App.debug){
        window.console.log(message);
    }
};

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
        localization: '../../js/localization'
    },

    deps: [
        'jquery',
        'underscore',
        'bootstrap',
        'jqueryUI'
    ],
    config: {
        i18n: {
            locale: (function(){
	            'use strict';
                try{
                    return App.config.locale;
                }catch(ex){
                    return 'en';
                }
            })()
        }
    }
});

require(['common-objects/contextResolver','i18n!localization/nls/common','i18n!localization/nls/download'],
    function (ContextResolver, commonStrings, downloadStrings) {

        'use strict';

        App.config.i18n = _.extend(commonStrings, downloadStrings);
        var load = function(){
            require(['backbone','app','common-objects/views/header'],function(Backbone, AppView, HeaderView){
                App.appView = new AppView().render();
                App.headerView = new HeaderView().render();
            });
        };
        ContextResolver.resolveServerProperties()
            .then(ContextResolver.resolveAccount)
            .then(function(){
                App.config.connected = true;
                return  ContextResolver.resolveWorkspaces();
            },function(response){
                App.config.connected = response.status !== 401;
                load();
            }).then(load);

    });

