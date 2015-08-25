/*global define,App*/
define([
    'backbone',
    'common-objects/common/singleton_decorator'
], function (Backbone, singletonDecorator) {
    'use strict';
    var Router = Backbone.Router.extend({

        routes: {
            'product/:workspaceId/:productId/:cameraX/:cameraY/:cameraZ/:pathToLoad(/:configSpec)': 'loadProduct',
            'assembly/:workspaceId/:partRevisionKey/:cameraX/:cameraY/:cameraZ': 'loadAssembly'
        },

        loadProduct: function (workspaceId, productId, cameraX, cameraY, cameraZ, pathToLoad, configSpec) {

            if (pathToLoad && productId) {

                App.config.workspaceId = workspaceId;
                App.config.productId = productId;
                App.config.productConfigSpec = configSpec || 'wip';

                App.config.defaultCameraPosition = {
                    x: parseFloat(cameraX || 0),
                    y: parseFloat(cameraY || 0),
                    z: parseFloat(cameraZ || 0)
                };

                App.instancesManager.clear();
                App.sceneManager.clear();
                App.sceneManager.resetCameraPlace();
                App.instancesManager.loadComponent({
                    getEncodedPath: function () {
                        return pathToLoad;
                    }
                });

            }
        },

        loadAssembly:function(workspaceId, partRevisionKey, cameraX, cameraY, cameraZ){

            if (partRevisionKey) {

                App.config.workspaceId = workspaceId;

                App.config.defaultCameraPosition = {
                    x: parseFloat(cameraX || 0),
                    y: parseFloat(cameraY || 0),
                    z: parseFloat(cameraZ || 0)
                };

                App.instancesManager.clear();
                App.sceneManager.clear();
                App.sceneManager.resetCameraPlace();
                App.instancesManager.loadAssembly(partRevisionKey);

            }
        },

        updateRoute: function () {
            /* nothing to do, but needs to be present*/
        }
    });
    Router = singletonDecorator(Router);
    return Router;
});
