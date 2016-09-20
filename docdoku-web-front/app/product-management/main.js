/*global _,require,window*/

var App = {};

require.config({
    baseUrl: 'js',
    shim: {
        jqueryUI: { deps: ['jquery'], exports: 'jQuery' },
        effects: { deps: ['jquery'], exports: 'jQuery' },
        popoverUtils: { deps: ['jquery'], exports: 'jQuery' },
        bootstrap:{ deps: ['jquery','jqueryUI'], exports: 'jQuery' },
        bootbox: { deps: ['jquery'], exports: 'jQuery' },
        datatables:{ deps: ['jquery'], exports: 'jQuery' },
        backbone: {deps: ['underscore', 'jquery'],exports: 'Backbone'},
        bootstrapCombobox:{deps:['jquery'],exports:'jQuery'},
        bootstrapSwitch:{deps:['jquery'],exports:'jQuery'},
        bootstrapDatepicker: {deps: ['jquery','bootstrap'], exports: 'jQuery'},
        unmask: { deps: ['jquery'], exports: 'jQuery' },
        unmaskConfig: { deps: ['unmask'], exports: 'jQuery' },
        inputValidity: { deps: ['jquery'], exports: 'jQuery' },
        'query-builder': { deps: ['jquery'], exports: 'jQuery' },
        selectize: { deps: ['jquery'], exports: 'jQuery' },
        datePickerLang: { deps: ['bootstrapDatepicker'], exports: 'jQuery'}
    },
    paths: {
        jquery: '../../bower_components/jquery/jquery',
        backbone: '../../bower_components/backbone/backbone',
        underscore: '../../bower_components/underscore/underscore',
        mustache: '../../bower_components/mustache/mustache',
        text: '../../bower_components/requirejs-text/text',
        i18n: '../../bower_components/requirejs-i18n/i18n',
        buzz: '../../bower_components/buzz/dist/buzz',
        bootstrap:'../../bower_components/bootstrap/docs/assets/js/bootstrap',
        bootbox:'../../bower_components/bootbox/bootbox',
        datatables:'../../bower_components/datatables/media/js/jquery.dataTables',
        jqueryUI: '../../bower_components/jqueryui/ui/jquery-ui',
        unmask:'../../bower_components/jquery-maskedinput/dist/jquery.maskedinput',
        bootstrapCombobox:'../../bower_components/bootstrap-combobox/js/bootstrap-combobox',
        bootstrapSwitch:'../../bower_components/bootstrap-switch/static/js/bootstrap-switch',
        bootstrapDatepicker:'../../bower_components/bootstrap-datepicker/js/bootstrap-datepicker',
        unorm:'../../bower_components/unorm/lib/unorm',
        moment:'../../bower_components/moment/min/moment-with-locales',
        momentTimeZone:'../../bower_components/moment-timezone/builds/moment-timezone-with-data',
        date:'../../bower_components/date.format/date.format',
        unmaskConfig:'../../js/utils/jquery.maskedinput-config',
        localization: '../../js/localization',
        modules: '../../js/modules',
        'common-objects': '../../js/common-objects',
        userPopover:'../../js/modules/user-popover-module/app',
        effects:'../../js/utils/effects',
        popoverUtils: '../../js/utils/popover.utils',
        inputValidity: '../../js/utils/input-validity',
        datatablesOsortExt: '../../js/utils/datatables.oSort.ext',
        utilsprototype:'../../js/utils/utils.prototype',
        async: '../../bower_components/async/lib/async',
        'query-builder': '../../bower_components/jQuery-QueryBuilder/dist/js/query-builder.standalone',
        'query-builder-options': '../../js/utils/query-builder-options',
        selectize: '../../bower_components/selectize/dist/js/standalone/selectize',
        datePickerLang: '../../bower_components/bootstrap-datepicker/js/locales/bootstrap-datepicker.fr'
    },

    deps:[
        'jquery',
        'underscore',
        'date',
        'bootstrap',
        'bootbox',
        'jqueryUI',
        'effects',
        'popoverUtils',
        'datatables',
        'datatablesOsortExt',
        'bootstrapCombobox',
        'bootstrapSwitch',
        'utilsprototype',
        'unmaskConfig',
        'inputValidity',
        'query-builder',
        'selectize',
        'datePickerLang'
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

require(['common-objects/contextResolver','i18n!localization/nls/common','i18n!localization/nls/product-management','common-objects/views/error'],
    function (ContextResolver,  commonStrings, productManagementStrings, ErrorView) {
	    'use strict';

        App.config.needAuthentication = true;
        App.config.i18n = _.extend(commonStrings, productManagementStrings);

        var match = /^#([^\/]+)/.exec(window.location.hash) || ['',''];
        App.config.workspaceId = decodeURIComponent(match[1] || '').trim();

        if(!App.config.workspaceId){
            new ErrorView({el:'#content'}).render404();
            return;
        }

        ContextResolver.resolveServerProperties()
            .then(ContextResolver.resolveAccount)
            .then(ContextResolver.resolveWorkspaces)
            .then(ContextResolver.resolveGroups)
            .then(ContextResolver.resolveUser)
            .then(function buildView(){
                require(['backbone','app','router','common-objects/views/header','modules/all'],function(Backbone, AppView, Router,HeaderView,Modules){
                    App.appView = new AppView().render();
                    App.headerView = new HeaderView().render();
                    App.router = Router.getInstance();
                    App.coworkersView = new Modules.CoWorkersAccessModuleView().render();
                    Backbone.history.start();
                });
            },function(xhr){
                new ErrorView({el:'#content'}).render({
                    title:xhr.statusText,
                    content:xhr.responseText
                });
            });
    });
