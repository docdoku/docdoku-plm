/*global require*/
'use strict';

var App = {
    debug:false
};

var APP_CONFIG = {
    workspaceId: /^#([^/]+)/.exec(window.location.hash)[1] || null,
    login: '',
    groups: [],
    contextPath: '',
    locale: localStorage.getItem('locale') || 'en'
};

if(!App.debug){
    console.log=function(){};
}

require.config({

    baseUrl: '../js/change-management',

    shim: {
        jqueryUI: { deps: ['jquery'], exports: 'jQuery' },
        effects: { deps: ['jquery'], exports: 'jQuery' },
        bootstrap: { deps: ['jquery', 'jqueryUI'], exports: 'jQuery' },
        datatables: { deps: ['jquery'], exports: 'jQuery' },
        bootstrapSwitch: {deps: ['jquery'], exports: 'jQuery'},
        backbone: {deps: ['underscore', 'jquery'],exports: 'Backbone'}
    },

    paths: {
        jquery: '../../bower_components/jquery/jquery',
        backbone: '../../bower_components/backbone/backbone',
        underscore: '../../bower_components/underscore/underscore',
        mustache: '../../bower_components/mustache/mustache',
        text: '../../bower_components/requirejs-text/text',
        i18n: '../../bower_components/requirejs-i18n/i18n',
        buzz: '../../bower_components/buzz/dist/buzz',
        bootstrap: '../../bower_components/bootstrap/docs/assets/js/bootstrap',
        datatables: '../../bower_components/datatables/media/js/jquery.dataTables',
        jqueryUI: '../../bower_components/jqueryui/ui/jquery-ui',
        bootstrapSwitch:'../../bower_components/bootstrap-switch/static/js/bootstrap-switch',
        date:'../../bower_components/date.format/date.format',
        localization: '../localization',
        modules: '../modules',
        'common-objects': '../common-objects',
        userPopover: 'modules/user-popover-module/app',
        effects: '../lib/effects',
        datatablesOsortExt: '../lib/datatables.oSort.ext',
        stringprototype: '../lib/string.prototype'
    },

    deps: [
        'jquery',
        'underscore',
        'date',
        'bootstrap',
        'bootstrapSwitch',
        'jqueryUI',
        'effects',
        'datatables',
        'datatablesOsortExt',
        'stringprototype'
    ],
    config: {
        i18n: {
            locale: (function(){
                try{
                    return APP_CONFIG.locale;
                }catch(ex){
                    return 'en';
                }
            })()
        }
    }
});


require(['common-objects/contextResolver','i18n!localization/nls/common','i18n!localization/nls/change-management'],
    function (ContextResolver,  commonStrings, changeManagementStrings) {
        APP_CONFIG.i18n = _.extend(commonStrings,changeManagementStrings);
        ContextResolver.resolve(function(){
            require(['backbone','app','router','common-objects/views/header','modules/all'],function(Backbone, AppView, Router,HeaderView,Modules){
                App.appView = new AppView().render();
                App.headerView = new HeaderView().render();
                App.router = Router.getInstance();
                App.coworkersView = new Modules.CoWorkersAccessModuleView().render();
                Backbone.history.start();
            });
        });
    });