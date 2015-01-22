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
            ':workspaceId/:productId/scene': 'scene',
            ':workspaceId/:productId/bom': 'bom',
            ':workspaceId/:productId/room/:key': 'joinCollaborative'
        },
        defaults: function (workspaceId, productId) {
            this.navigate(workspaceId+'/'+productId+'/scene',{trigger:true});
        },
        scene:function(workspaceId, productId){
            App.appView.sceneMode();
        },
        bom:function(workspaceId, productId){
            App.appView.initBom();
        },
        joinCollaborative: function (workspaceId, productId, key) {
            if (!App.collaborativeView.isMaster) {
                App.appView.requestJoinRoom(key);
            }
        }
    });
    Router = singletonDecorator(Router);
    return Router;
});
