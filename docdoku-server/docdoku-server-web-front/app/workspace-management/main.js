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

    baseUrl: '../js/workspace-management',

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
        date_picker_lang: { deps: ['bootstrapDatepicker'], exports: 'jQuery'},
        d3:{deps:[],exports:'window'},
        nvd3:{deps:['d3'],exports:'window'},
        legend:{deps:['nvd3','d3']},
        pie:{deps:['nvd3','d3']},
        pieChart:{deps:['nvd3','d3']},
        discreteBar:{deps:['nvd3','d3']},
        discreteBarChart:{deps:['nvd3','d3']},
        nvutils:{deps:['nvd3','d3']},
        tooltip:{deps:['nvd3','d3'],exports:'window'},
        fisheye:{deps:['nvd3','d3']},
        helpers:{deps:['nvd3','d3']},
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
        unmaskConfig:'../utils/jquery.maskedinput-config',
        localization: '../localization',
        modules: '../modules',
        'common-objects': '../common-objects',
        effects: '../utils/effects',
        popoverUtils: '../utils/popover.utils',
        inputValidity: '../utils/input-validity',
        datatablesOsortExt: '../utils/datatables.oSort.ext',
        utilsprototype: '../utils/utils.prototype',
        userPopover: 'modules/user-popover-module/app',
        async: '../../bower_components/async/lib/async',
        date_picker_lang: '../../bower_components/bootstrap-datepicker/js/locales/bootstrap-datepicker.fr',
        d3:'../lib/charts/nv3d/lib/d3.v2',
        nvd3:'../lib/charts/nv3d/nv.d3',
        legend:'../lib/charts/nv3d/src/models/legend',
        pie:'../lib/charts/nv3d/src/models/pie',
        pieChart:'../lib/charts/nv3d/src/models/pieChart',
        discreteBar:'../lib/charts/nv3d/src/models/discreteBar',
        discreteBarChart:'../lib/charts/nv3d/src/models/discreteBarChart',
        nvutils:'../lib/charts/nv3d/src/utils',
        fisheye:'../lib/charts/nv3d/lib/fisheye',
        tooltip:'../lib/charts/nv3d/custom/tooltip',
        helpers:'../lib/charts/helpers'
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
        'date_picker_lang',
        'd3',
        'nvd3',
        'legend',
        'pie',
        'pieChart',
        'discreteBar',
        'discreteBarChart',
        'nvutils',
        'fisheye',
        'tooltip',
        'helpers'
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

