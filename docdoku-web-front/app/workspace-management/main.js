/*global _,require,window*/


var App = {
    debug:false,
	config:{
		login: '',
		groups: [],
		contextPath: '',
		locale: window.localStorage.getItem('locale') || 'en',
        needAuthentication:true
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
        effects: { deps: ['jquery'], exports: 'jQuery' },
        popoverUtils: { deps: ['jquery'], exports: 'jQuery' },
        inputValidity: { deps: ['jquery'], exports: 'jQuery' },
        bootstrap: { deps: ['jquery', 'jqueryUI'], exports: 'jQuery' },
        bootbox: { deps: ['jquery'], exports: 'jQuery' },
        datatables: { deps: ['jquery'], exports: 'jQuery' },
	    unmask: { deps: ['jquery'], exports: 'jQuery' },
	    unmaskConfig: { deps: ['unmask','jquery'], exports: 'jQuery' },
        bootstrapSwitch: { deps: ['jquery'], exports: 'jQuery'},
        bootstrapDatepicker: {deps: ['jquery','bootstrap'], exports: 'jQuery'},
        backbone: { deps: ['underscore', 'jquery'], exports: 'Backbone'},
        datePickerLang: { deps: ['bootstrapDatepicker'], exports: 'jQuery'},
        d3:{deps:[],exports:'window'},
        nvd3:{deps:['d3'],exports:'window'},
        helpers:{deps:['nvd3','d3']}
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
        bootbox:'../../bower_components/bootbox/bootbox',
        datatables: '../../bower_components/datatables/media/js/jquery.dataTables',
        jqueryUI: '../../bower_components/jqueryui/ui/jquery-ui',
        unmask:'../../bower_components/jquery-maskedinput/dist/jquery.maskedinput',
        bootstrapSwitch:'../../bower_components/bootstrap-switch/static/js/bootstrap-switch',
        bootstrapDatepicker:'../../bower_components/bootstrap-datepicker/js/bootstrap-datepicker',
        date:'../../bower_components/date.format/date.format',
        unorm:'../../bower_components/unorm/lib/unorm',
        moment:'../../bower_components/moment/min/moment-with-locales',
        momentTimeZone:'../../bower_components/moment-timezone/builds/moment-timezone-with-data',
        unmaskConfig:'../../js/utils/jquery.maskedinput-config',
        localization: '../../js/localization',
        modules: '../../js/modules',
        'common-objects': '../../js/common-objects',
        effects: '../../js/utils/effects',
        popoverUtils: '../../js/utils/popover.utils',
        inputValidity: '../../js/utils/input-validity',
        datatablesOsortExt: '../../js/utils/datatables.oSort.ext',
        utilsprototype: '../../js/utils/utils.prototype',
        userPopover: '../../js/modules/user-popover-module/app',
        async: '../../bower_components/async/lib/async',
        datePickerLang: '../../bower_components/bootstrap-datepicker/js/locales/bootstrap-datepicker.fr',
        d3:'../../bower_components/d3/d3',
        nvd3:'../../bower_components/nvd3/build/nv.d3',
        'charts-helpers':'../../js/utils/charts-helpers'
    },

    deps: [
        'jquery',
        'underscore',
        'date',
        'bootstrap',
        'bootbox',
        'bootstrapSwitch',
        'jqueryUI',
        'effects',
        'popoverUtils',
        'inputValidity',
        'datatables',
        'datatablesOsortExt',
        'unmaskConfig',
        'utilsprototype',
        'datePickerLang',
        'd3',
        'nvd3'
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

require(['common-objects/contextResolver','i18n!localization/nls/common','i18n!localization/nls/workspace-management'],
    function (ContextResolver, commonStrings, workspaceManagementStrings) {

        'use strict';

        App.config.i18n = _.extend(commonStrings,workspaceManagementStrings);

        ContextResolver.resolveServerProperties()
            .then(ContextResolver.resolveAccount)
            .then(ContextResolver.resolveWorkspaces)
            .then(function buildView(){
                require(['backbone','app','router','common-objects/views/header','modules/all'],function(Backbone, AppView, Router,HeaderView,Modules){

                    App.appView = new AppView();
                    App.headerView = new HeaderView();

                    if(!App.config.admin){
                        App.headerView.setCoWorkersView(Modules.CoWorkersAccessModuleView);
                    }

                    App.router = Router.getInstance();
                    Backbone.history.start();
                });
            });
    });

