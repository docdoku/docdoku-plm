/*global define,App*/
define([
    'backbone',
    'common-objects/common/singleton_decorator'
],
function (Backbone, singletonDecorator) {
    'use strict';
    var Router = Backbone.Router.extend({
        routes: {
            ':workspaceId/:productId': 'defaults',
            ':workspaceId/:productId/scene(/camera/:camera)(/target/:target)(/up/:up)': 'scene',
            ':workspaceId/:productId/bom': 'bom',
            ':workspaceId/:productId/room/:key': 'joinCollaborative'
        },
        defaults: function (workspaceId, productId) {
            this.navigate(workspaceId+'/'+productId+'/scene',{trigger:true});
        },
        scene:function(workspaceId, productId, camera, target, up){
            App.appView.sceneMode();
            if (camera && target && up) {
                var c = camera.split(';');
                var t = target.split(';');
                var u = up.split(';');
                App.sceneManager.setControlsContext({
                    target: new THREE.Vector3(parseFloat(t[0]), parseFloat(t[1]), parseFloat(t[2])),
                    camPos: new THREE.Vector3(parseFloat(c[0]), parseFloat(c[1]), parseFloat(c[2])),
                    camOrientation: new THREE.Vector3(parseFloat(u[0]), parseFloat(u[1]), parseFloat(u[2]))
                });
            }
        },
        bom:function(workspaceId, productId){
            App.appView.initBom();
        },
        joinCollaborative: function (workspaceId, productId, key) {
            if (!App.collaborativeView.isMaster) {
                App.appView.requestJoinRoom(key);
            }
        },

        updateRoute: function (context) {
            if(App.appView.isInBomMode()){
                return;
            }
            var c = context.camPos.toArray();
            var t = context.target.toArray();
            var u = context.camOrientation.toArray();
            var positionPrecision = 2;
            this.navigate(
                App.config.workspaceId+'/'+App.config.productId+'/scene' +

                '/camera/'
                + c[0].toFixed(positionPrecision) + ';'
                + c[1].toFixed(positionPrecision) + ';'
                + c[2].toFixed(positionPrecision) +
                '/target/'
                + t[0].toFixed(positionPrecision) + ';'
                + t[1].toFixed(positionPrecision) + ';'
                + t[2].toFixed(positionPrecision) +
                '/up/'
                + u[0].toFixed(positionPrecision) + ';'
                + u[1].toFixed(positionPrecision) + ';'
                + u[2].toFixed(positionPrecision)
                ,
                {trigger: false});
        }
    });
    Router = singletonDecorator(Router);
    return Router;
});
