/*global _,require,window*/

var App = {};

require.config({
    baseUrl: 'js',
    shim: {
        jqueryUI: { deps: ['jquery'], exports: 'jQuery' },
        effects: { deps: ['jquery'], exports: 'jQuery' },
        popoverUtils: { deps: ['jquery'], exports: 'jQuery' },
        inputValidity: { deps: ['jquery'], exports: 'jQuery' },
        bootstrap:{ deps: ['jquery','jqueryUI'], exports: 'jQuery' },
        datatables:{ deps: ['jquery'], exports: 'jQuery' },
        backbone: {deps: ['underscore', 'jquery'],exports: 'Backbone'},
        bootstrapCombobox:{deps:['jquery'],exports:'jQuery'},
        bootstrapSwitch:{deps:['jquery'],exports:'jQuery'},
        bootstrapDatepicker: {deps: ['jquery','bootstrap'], exports: 'jQuery'},
        pointerlockcontrols:{deps:['threecore'],exports:'THREE'},
        trackballcontrols:{deps:['threecore'],exports:'THREE'},
        orbitcontrols:{deps:['threecore'],exports:'THREE'},
        transformcontrols:{deps:['threecore'],exports:'THREE'},
        binaryloader:{deps:['threecore'],exports:'THREE'},
        colladaloader:{deps:['threecore'],exports:'THREE'},
        stlloader:{deps:['threecore'],exports:'THREE'},
        objloader:{deps:['threecore'],exports:'THREE'},
        mtlloader:{deps:['threecore'],exports:'THREE'},
        buffergeometryutils:{deps:['threecore'],exports:'THREE'},
        typeface : { deps: ['threecore'], exports: 'window' },
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
        datatables:'../../bower_components/datatables/media/js/jquery.dataTables',
        unorm:'../../bower_components/unorm/lib/unorm',
        moment:'../../bower_components/moment/min/moment-with-locales',
        momentTimeZone:'../../bower_components/moment-timezone/builds/moment-timezone-with-data',
        threecore:'../../bower_components/threejs/build/three',
        jqueryUI: '../../bower_components/jqueryui/ui/jquery-ui',
        async: '../../bower_components/async/lib/async',
        tween:'../../bower_components/tweenjs/src/Tween',
        bootstrapCombobox:'../../bower_components/bootstrap-combobox/js/bootstrap-combobox',
        bootstrapSwitch:'../../bower_components/bootstrap-switch/static/js/bootstrap-switch',
        bootstrapDatepicker:'../../bower_components/bootstrap-datepicker/js/bootstrap-datepicker',
        date:'../../bower_components/date.format/date.format',
        dat:'../../bower_components/dat.gui/dat.gui',
        localization: '../../js/localization',
        modules: '../../js/modules',
        'common-objects': '../../js/common-objects',
        userPopover:'../../js/modules/user-popover-module/app',
        effects:'../../js/utils/effects',
        popoverUtils: '../../js/utils/popover.utils',
        inputValidity: '../../js/utils/input-validity',
        datatablesOsortExt: '../../js/utils/datatables.oSort.ext',
        utilsprototype:'../../js/utils/utils.prototype',
        pointerlockcontrols:'../../js/dmu/controls/PointerLockControls',
        trackballcontrols:'../../js/dmu/controls/TrackballControls',
        orbitcontrols:'../../js/dmu/controls/OrbitControls',
        transformcontrols:'../../js/dmu/controls/TransformControls',
        binaryloader:'../../js/dmu/loaders/BinaryLoader',
        colladaloader:'../../js/dmu/loaders/ColladaLoader',
        stlloader:'../../js/dmu/loaders/STLLoader',
        objloader:'../../js/dmu/loaders/OBJLoader',
        mtlloader:'../../js/dmu/loaders/MTLLoader',
        buffergeometryutils: '../../js/dmu/utils/BufferGeometryUtils',
        stats:'../../js/dmu/utils/Stats',
        typeface:'../../js/lib/helvetiker_regular.typeface',
        selectize: '../../bower_components/selectize/dist/js/standalone/selectize',
        datePickerLang: '../../bower_components/bootstrap-datepicker/js/locales/bootstrap-datepicker.fr'
    },

    deps:[
        'jquery',
        'underscore',
        'date',
        'jqueryUI',
        'bootstrap',
        'effects',
        'popoverUtils',
        'datatables',
        'datatablesOsortExt',
        'bootstrapCombobox',
        'bootstrapSwitch',
        'utilsprototype',
        'threecore',
        'pointerlockcontrols',
        'trackballcontrols',
        'orbitcontrols',
        'transformcontrols',
        'binaryloader',
        'colladaloader',
        'stlloader',
        'objloader',
        'mtlloader',
        'buffergeometryutils',
        'stats',
        'dat',
        'tween',
        'inputValidity',
        'typeface',
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

require(['common-objects/contextResolver','i18n!localization/nls/common','i18n!localization/nls/product-structure', 'common-objects/views/error'],
    function (ContextResolver,  commonStrings, productStructureStrings, ErrorView) {
	    'use strict';

        App.config.i18n = _.extend(commonStrings,productStructureStrings);
        App.config.needAuthentication = true;

        var match = /^#([^\/]+)/.exec(window.location.hash) || ['',''];
        App.config.workspaceId = decodeURIComponent(match[1] || '').trim();

        if(!App.config.workspaceId){
            new ErrorView({el:'#content'}).render404();
            return;
        }

        App.config.productId = decodeURIComponent(window.location.hash.split('/')[1]).trim() || null;

        App.WorkerManagedValues = {
            maxInstances: 500,
            maxAngle: Math.PI / 2,
            maxDist: 5E10,
            minProjectedSize: 0.000001,//100,
            distanceRating: 0.6,//0.7,
            angleRating: 0.4,//0.6,//0.5,
            volRating: 1.0//0.7
        };

        App.SceneOptions = {
            grid: false,
            zoomSpeed: 1.2,
            rotateSpeed: 1.0,
            panSpeed: 0.3,
            cameraNear: 0.1,
            cameraFar: 5E4,
            defaultCameraPosition: {x: -1000, y: 800, z: 1100},
            defaultTargetPosition: {x: 0, y: 0, z: 0},
            ambientLightColor:0xffffff,
            cameraLight1Color:0xbcbcbc,
            cameraLight2Color:0xffffff,
            transformControls:true
        };

        ContextResolver.resolveServerProperties()
            .then(ContextResolver.resolveAccount)
            .then(ContextResolver.resolveWorkspaces)
            .then(ContextResolver.resolveGroups)
            .then(ContextResolver.resolveUser)
            .then(function(){
                require(['backbone','app','router','common-objects/views/header','modules/all'],function(Backbone, AppView, Router,HeaderView,Modules){
                    App.appView = new AppView().render();
                    App.headerView = new HeaderView().render();
                    App.router = Router.getInstance();
                    App.coworkersView = new Modules.CoWorkersAccessModuleView().render();
                    Backbone.history.start();
                    App.appView.initModules();
                });
            },function(xhr){
                new ErrorView({el:'#content'}).render({
                    title:xhr.statusText,
                    content:xhr.responseText
                });
            });
    });
