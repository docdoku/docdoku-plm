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
                ':workspaceId/:productId/room/:key': 'joinCollaborative'
            },
            defaults: function (workspaceId, productId) {

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
