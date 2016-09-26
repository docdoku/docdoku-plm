/*global _,require,window*/

var App = {};

require.config({

    baseUrl: 'js',

    shim: {
        jqueryUI: { deps: ['jquery'], exports: 'jQuery' },
        bootstrap: { deps: ['jquery', 'jqueryUI'], exports: 'jQuery' },
        backbone: { deps: ['underscore', 'jquery'], exports: 'Backbone'},
        pointerlockcontrols: {deps: ['threecore'], exports: 'THREE'},
        trackballcontrols: {deps: ['threecore'], exports: 'THREE'},
        orbitcontrols: {deps: ['threecore'], exports: 'THREE'},
        binaryloader: {deps: ['threecore'], exports: 'THREE'},
        colladaloader: {deps: ['threecore'], exports: 'THREE'},
        stlloader: {deps: ['threecore'], exports: 'THREE'},
        objloader: {deps: ['threecore'], exports: 'THREE'},
        mtlloader:{deps:['threecore'],exports:'THREE'},
        buffergeometryutils: {deps: ['threecore'], exports: 'THREE'},
        popoverUtils: {deps: ['jquery'], exports: 'jQuery'}
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
        pluginDetect:'../../js/lib/plugin-detect',
        threecore: '../../bower_components/threejs/build/three',
        pointerlockcontrols: '../../js/dmu/controls/PointerLockControls',
        trackballcontrols: '../../js/dmu/controls/TrackballControls',
        orbitcontrols: '../../js/dmu/controls/OrbitControls',
        binaryloader: '../../js/dmu/loaders/BinaryLoader',
        colladaloader: '../../js/dmu/loaders/ColladaLoader',
        buffergeometryutils: '../../js/dmu/utils/BufferGeometryUtils',
        stlloader: '../../js/dmu/loaders/STLLoader',
        objloader: '../../js/dmu/loaders/OBJLoader',
        mtlloader: '../../js/dmu/loaders/MTLLoader',
        popoverUtils: '../../js/utils/popover.utils',
        moment: '../../bower_components/moment/min/moment-with-locales',
        momentTimeZone: '../../bower_components/moment-timezone/builds/moment-timezone-with-data',
    },

    deps: [
        'jquery',
        'underscore',
        'bootstrap',
        'jqueryUI',
        'pluginDetect',
        'popoverUtils',
        'threecore',
        'pointerlockcontrols',
        'trackballcontrols',
        'orbitcontrols',
        'binaryloader',
        'colladaloader',
        'stlloader',
        'objloader',
        'mtlloader',
        'buffergeometryutils'
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

        App.config.SceneOptions = {
            zoomSpeed: 1.2,
            rotateSpeed: 1.0,
            panSpeed: 0.3,
            cameraNear: 0.1,
            cameraFar: 5E4,
            defaultCameraPosition: {x: 0, y: 50, z: 200},
            ambientLightColor: 0xffffff,
            cameraLight1Color: 0xbcbcbc,
            cameraLight2Color: 0xffffff
        };

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

