/*global require*/
'use strict';

var APP_CONFIG = {
    workspaceId: /^#([^/]+)/.exec(window.location.hash)[1] || null,
    productId: window.location.hash.split('/')[1] || null,
    login: '',
    groups: [],
    contextPath: '',
    locale: localStorage.getItem('locale') || 'en'
};

var App = {
    debug: false,
    instancesManager: null,
    sceneManager: null,
    logLevel: {ALL: -1, DEBUG: 0, INFO: 1, WARN: 2, ERROR: 3, FATAL: 4, OFF: 5},
    logType: {ALL: -1, DEFAULT: 0, WORKER: 1, WEBSOCKET: 2},

    WorkerManagedValues: {
        maxInstances: 500,
        maxAngle: Math.PI / 4,
        maxDist: 100000,
        minProjectedSize: 0.000001,//100,
        distanceRating: 0.6,//0.7,
        angleRating: 0.4,//0.6,//0.5,
        volRating: 1.0//0.7
    },

    SceneOptions: {
        grid: false,
        skeleton: true,
        zoomSpeed: 1.2,
        rotateSpeed: 1.0,
        panSpeed: 0.3,
        cameraNear: 1,
        cameraFar: 5E4,
        defaultCameraPosition: {x: -1000, y: 800, z: 1100},
        defaultTargetPosition: {x: 0, y: 0, z: 0},
        transformControls:false
    }

};

if(!App.debug){
    console.log=function(){};
}

require.config({
    baseUrl: '../js/product-structure',
    shim: {
        jqueryUI: { deps: ['jquery'], exports: 'jQuery' },
        bootstrap: { deps: ['jquery', 'jqueryUI'], exports: 'jQuery' },
        backbone: {deps: ['underscore', 'jquery'], exports: 'Backbone'},
        pointerlockcontrols: {deps: ['threecore'], exports: 'THREE'},
        trackballcontrols: {deps: ['threecore'], exports: 'THREE'},
        orbitcontrols: {deps: ['threecore'], exports: 'THREE'},
        binaryloader: {deps: ['threecore'], exports: 'THREE'},
        colladaloader: {deps: ['threecore'], exports: 'THREE'},
        stlloader: {deps: ['threecore'], exports: 'THREE'},
        buffergeometryutils: {deps: ['threecore'], exports: 'THREE'},
    },
    paths: {
        jquery: '../../bower_components/jquery/jquery',
        backbone: '../../bower_components/backbone/backbone',
        underscore: '../../bower_components/underscore/underscore',
        mustache: '../../bower_components/mustache/mustache',
        text: '../../bower_components/requirejs-text/text',
        i18n: '../../bower_components/requirejs-i18n/i18n',
        threecore: '../../bower_components/threejs/build/three',
        async: '../../bower_components/async/lib/async',
        tween:'../../bower_components/tweenjs/src/Tween',
        date:'../../bower_components/date.format/date.format',
        dat:'../../bower_components/dat.gui/dat.gui',
        localization: '../localization',
        'common-objects': '../common-objects',
        pointerlockcontrols: 'dmu/controls/PointerLockControls',
        trackballcontrols: 'dmu/controls/TrackballControls',
        orbitcontrols: 'dmu/controls/OrbitControls',
        binaryloader: 'dmu/loaders/BinaryLoader',
        colladaloader: 'dmu/loaders/ColladaLoader',
        buffergeometryutils: 'dmu/utils/BufferGeometryUtils',
        stlloader: 'dmu/loaders/STLLoader',
        stats:'dmu/utils/Stats'
    },

    deps: [
        'threecore',
        'pointerlockcontrols',
        'trackballcontrols',
        'orbitcontrols',
        'binaryloader',
        'colladaloader',
        'stlloader',
        'buffergeometryutils',
        'stats',
        'dat',
        'tween'
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

require(['common-objects/contextResolver','i18n!localization/nls/common','i18n!localization/nls/product-structure'],
    function (ContextResolver,  commonStrings, productStructureStrings) {
        APP_CONFIG.i18n = _.extend(commonStrings,productStructureStrings);
        ContextResolver.resolve(function(){
            require(['backbone','frameRouter', 'dmu/SceneManager','dmu/InstancesManager'],function(Backbone,  Router,SceneManager,InstancesManager){
                App.instancesManager = new InstancesManager();
                App.sceneManager = new SceneManager();
                App.sceneManager.init();
                App.router = Router.getInstance();
                Backbone.history.start();
            });
        });
    });