/*global define,App*/
define([
        "backbone",
        "common-objects/common/singleton_decorator"
    ],
    function (Backbone, singletonDecorator) {
        var Router = Backbone.Router.extend({
            routes: {
                ":workspaceId/:productId/:cameraX/:cameraY/:cameraZ/:pathToLoad(/:configSpec)": "load"
            },
            load: function (workspaceId, productId, cameraX, cameraY, cameraZ, pathToLoad, configSpec) {

                if (pathToLoad && productId) {

                    APP_CONFIG.workspaceId = workspaceId;
                    APP_CONFIG.productId = productId;
                    APP_CONFIG.configSpec = configSpec || 'latest';

                    APP_CONFIG.defaultCameraPosition = {
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
            }
        });
        Router = singletonDecorator(Router);
        return Router;
    });
