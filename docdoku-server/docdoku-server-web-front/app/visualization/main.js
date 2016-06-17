/*global $,require,window,_*/
var App = {
    debug: false,

	config:{
		workspaceId: decodeURIComponent(/^#(product|assembly)\/([^\/]+)/.exec(window.location.hash)[2]).trim() || null,
		productId: decodeURIComponent(window.location.hash.split('/')[2]).trim() || null,
		login: '',
		groups: [],
		contextPath: '',
		locale: window.localStorage.getItem('locale') || 'en',
        needAuthentication:true
	},

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
        zoomSpeed: 1.2,
        rotateSpeed: 1.0,
        panSpeed: 0.3,
        cameraNear: 1,
        cameraFar: 5E4,
        defaultCameraPosition: {x: -1000, y: 800, z: 1100},
        defaultTargetPosition: {x: 0, y: 0, z: 0},
        ambientLightColor:0xffffff,
        cameraLight1Color:0xbcbcbc,
        cameraLight2Color:0xffffff,
        transformControls:false
    }

};

App.log=function(message,colorType){
    'use strict';
    if(App.debug){
        if(colorType){
            switch (colorType) {
                case 'WS' :
                    window.console.log('%c [WS] ' + message, 'background: #222; color: #bada55','background: none; color:inherit');
                    break;
                case 'IM' :
                    window.console.log('%c [InstancesManager] ' + message, 'background: #206963; color: #bada55','background: none; color:inherit');
                    break;
                case 'SM' :
                    window.console.log('%c [SceneManager] ' + message, 'background: #275217; color: #bada55','background: none; color:inherit');
                    break;
                case 'PTV' :
                    window.console.log('%c [PartsTreeView] ' + message, 'background: #3C4C52; color: #bada55','background: none; color:inherit');
                    break;
                default :
                    window.console.log(message, 'background: #888; color: #bada55','background: none; color:inherit');
                    break;
            }
        }else{
            window.console.log(message);
        }
    }
};

require.config({
    baseUrl: '../product-structure/js',
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
        objloader: {deps: ['threecore'], exports: 'THREE'},
        mtlloader:{deps:['threecore'],exports:'THREE'},
        buffergeometryutils: {deps: ['threecore'], exports: 'THREE'}
    },
    paths: {
        jquery: '../../bower_components/jquery/jquery',
        jqueryUI: '../../bower_components/jqueryui/ui/jquery-ui',
        backbone: '../../bower_components/backbone/backbone',
        bootstrap:'../../bower_components/bootstrap/docs/assets/js/bootstrap',
        underscore: '../../bower_components/underscore/underscore',
        mustache: '../../bower_components/mustache/mustache',
        text: '../../bower_components/requirejs-text/text',
        i18n: '../../bower_components/requirejs-i18n/i18n',
        threecore: '../../bower_components/threejs/build/three',
        async: '../../bower_components/async/lib/async',
        tween:'../../bower_components/tweenjs/src/Tween',
        date:'../../bower_components/date.format/date.format',
        dat:'../../bower_components/dat.gui/dat.gui',
        localization: '../../js/localization',
        moment:'../../bower_components/moment/min/moment-with-locales',
        momentTimeZone:'../../bower_components/moment-timezone/builds/moment-timezone-with-data',
        'common-objects': '../../js/common-objects',
        pointerlockcontrols: '../../js/dmu/controls/PointerLockControls',
        trackballcontrols: '../../js/dmu/controls/TrackballControls',
        orbitcontrols: '../../js/dmu/controls/OrbitControls',
        binaryloader: '../../js/dmu/loaders/BinaryLoader',
        colladaloader: '../../js/dmu/loaders/ColladaLoader',
        buffergeometryutils: '../../js/dmu/utils/BufferGeometryUtils',
        stlloader: '../../js/dmu/loaders/STLLoader',
        objloader: '../../js/dmu/loaders/OBJLoader',
        mtlloader: '../../js/dmu/loaders/MTLLoader',
        stats:'../../js/dmu/utils/Stats',
        utilsprototype:'../../js/utils/utils.prototype'
    },

    deps: [
        'threecore',
        'pointerlockcontrols',
        'trackballcontrols',
        'orbitcontrols',
        'binaryloader',
        'colladaloader',
        'stlloader',
        'objloader',
        'mtlloader',
        'buffergeometryutils',
        'stats',
        'dat',
        'tween',
        'utilsprototype',
        'bootstrap'
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

require(['common-objects/contextResolver','i18n!localization/nls/common','i18n!localization/nls/product-structure'],
function (ContextResolver,  commonStrings, productStructureStrings) {
    'use strict';
    App.config.i18n = _.extend(commonStrings,productStructureStrings);
    ContextResolver.resolveServerProperties()
        .then(ContextResolver.resolveAccount)
        .then(ContextResolver.resolveWorkspaces)
        .then(ContextResolver.resolveGroups)
        .then(ContextResolver.resolveUser)
        .then(function(){
            require(['backbone', 'frameRouter', 'dmu/SceneManager','dmu/InstancesManager'],function(Backbone,  Router,SceneManager,InstancesManager){
                App.$SceneContainer = $('div#frameWorkspace');
                App.instancesManager = new InstancesManager();
                App.sceneManager = new SceneManager();
                App.sceneManager.init();
                App.router = Router.getInstance();
                Backbone.history.start();
            });
        });
});
