/*global _,require,window*/

var App = {};

require.config({

    baseUrl: 'main/js',

    shim: {
        jqueryUI: { deps: ['jquery'], exports: 'jQuery' },
        bootstrap: { deps: ['jquery', 'jqueryUI'], exports: 'jQuery' },
        backbone: { deps: ['underscore', 'jquery'], exports: 'Backbone'},
        trackballcontrols:{deps:['threecore'],exports:'THREE'},
        binaryloader:{deps:['threecore'],exports:'THREE'}
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
        threecore:'../../bower_components/threejs/build/three',
        tween:'../../bower_components/tweenjs/src/Tween',
        trackballcontrols:'../../js/dmu/controls/TrackballControls',
        binaryloader:'../../js/dmu/loaders/BinaryLoader',
        urlUtils:'../../js/utils/url-utils'

    },

    deps: [
        'jquery',
        'underscore',
        'bootstrap',
        'jqueryUI',
        'threecore',
        'tween',
        'trackballcontrols',
        'binaryloader'
    ],
    config: {
        i18n: {
            locale: (function(){
                'use strict';
                try{
                    return window.localStorage.getItem('locale') || 'en';
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

        App.config.needAuthentication = false;
        App.config.i18n = _.extend(commonStrings, indexStrings);

        App.SceneOptions =  {
            zoomSpeed: 3,
            rotateSpeed: 3.0,
            panSpeed: 0.3,
            cameraNear: 1,
            cameraFar: 5E4,
            defaultCameraPosition: {x: 0, y:0, z:0},
            startCameraPosition: {x: 100, y: 2500, z: 2500},
            endCameraPosition: {x: 0, y: 250, z: 250},
            defaultTargetPosition: {x: 0, y: 0, z: 0}
        };

        ContextResolver.resolveServerProperties()
            .then(function(){
                ContextResolver.resolveAccount().then(function(){
                    window.location.href = App.config.contextPath + '/workspace-management/';
                }, null);
            })
            .then(function buildView(){
                require(['backbone','app','router','common-objects/views/header'],function(Backbone, AppView, Router,HeaderView){
                    App.appView = new AppView();
                    App.headerView = new HeaderView();
                    App.appView.render();
                    App.headerView.render();
                    App.router = Router.getInstance();
                    Backbone.history.start();
                });
            });
    });

