/*global define,App*/
define([
    'backbone',
    'common-objects/common/singleton_decorator'
], function (Backbone, singletonDecorator) {
    'use strict';
    var Router = Backbone.Router.extend({
        routes: {
            ':workspaceId/:productId/:cameraX/:cameraY/:cameraZ/:pathToLoad(/:configSpec)': 'load'
        },
        load: function (workspaceId, productId, cameraX, cameraY, cameraZ, pathToLoad, configSpec) {

            if (pathToLoad && productId) {

                App.config.workspaceId = workspaceId;
                App.config.productId = productId;
                App.config.configSpec = configSpec || 'wip';

                App.config.defaultCameraPosition = {
                    x: parseFloat(cameraX || 0),
                    y: parseFloat(cameraY || 0),
                    z: parseFloat(cameraZ || 0)
                };

                App.instancesManager.clear();
                App.sceneManager.clear();

                App.sceneManager.resetCameraPlace();

                App.instancesManager.loadComponent({
                    getPath: function () {
                        return pathToLoad;
                    }
                });

            }
        },
        updateRoute: function () {
            /* nothing to do, but needs to be present*/
        }
    });
    Router = singletonDecorator(Router);
    return Router;
});
