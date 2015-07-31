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
            ':workspaceId/:productId/config-spec/:configSpecType': 'defaults',
            ':workspaceId/:productId/config-spec/:configSpecType/scene(/camera/:camera)(/target/:target)(/up/:up)': 'scene',
            ':workspaceId/:productId/config-spec/:configSpecType/bom': 'bom',
            ':workspaceId/:productId/config-spec/:configSpecType/room/:key': 'joinCollaborative'
        },

        defaults: function (workspaceId, productId, configSpecType) {
            if(!configSpecType){
                configSpecType = 'wip';
            }
            this.navigate(workspaceId+'/'+productId+'/config-spec/'+configSpecType+'/scene',{trigger:true});
        },

        scene:function(workspaceId, productId, configSpecType, camera, target, up){
            App.config.productConfigSpec = configSpecType;
            App.appView.sceneMode();
            if (camera && target && up) {
                var c = camera.split(';');
                var t = target.split(';');
                var u = up.split(';');
                App.sceneManager.setControlsContext({
                    target: {x:parseFloat(t[0]),y: parseFloat(t[1]),z: parseFloat(t[2])},
                    camPos: {x:parseFloat(c[0]),y: parseFloat(c[1]),z: parseFloat(c[2])},
                    camOrientation: {x:parseFloat(u[0]),y:parseFloat(u[1]),z: parseFloat(u[2])}
                });
            }
        },

        bom:function(workspaceId, productId, configSpecType){
            App.config.productConfigSpec = configSpecType;
            App.appView.bomMode();
            App.appView.once('app:ready',function() {
                App.partsTreeView.$el.trigger('load:root');
            });
        },

        joinCollaborative: function (workspaceId, productId, configSpecType,  key) {
            App.config.productConfigSpec = configSpecType;
            App.appView.sceneMode();
            if (!App.collaborativeView.isMaster) {
                App.appView.once('app:ready',function(){
                    App.collaborativeController.sendJoinRequest(key);
                });
            }
        },

        updateRoute: function (context) {

            if(!App.collaborativeView.roomKey){

                var c = context.camPos.toArray();
                var t = context.target.toArray();
                var u = context.camOrientation.toArray();
                var positionPrecision = 2;

                this.navigate(
                    App.config.workspaceId + '/' + App.config.productId+
                    '/config-spec/' + App.config.productConfigSpec +
                    '/scene/camera/' +
                    c[0].toFixed(positionPrecision) + ';' +
                    c[1].toFixed(positionPrecision) + ';' +
                    c[2].toFixed(positionPrecision) +
                    '/target/' +
                    t[0].toFixed(positionPrecision) + ';' +
                    t[1].toFixed(positionPrecision) + ';' +
                    t[2].toFixed(positionPrecision) +
                    '/up/' +
                    u[0].toFixed(positionPrecision) + ';' +
                    u[1].toFixed(positionPrecision) + ';' +
                    u[2].toFixed(positionPrecision),{
                    trigger: false
                });

            }
        }
    });
    Router = singletonDecorator(Router);
    return Router;
});
