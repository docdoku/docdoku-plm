/*global define,App,$*/
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
                $('#container').css({opacity:0});
                App.instancesManager.loadProduct(pathToLoad,function(){
                    $('#content').remove();
                    $('#container').css({opacity:1});
                    setTimeout(App.instancesManager.planNewEval,100);
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
                $('#container').css({opacity:0});
                App.instancesManager.loadAssembly(partRevisionKey,function(){
                    $('#content').remove();
                    $('#container').css({opacity:1});
                    setTimeout(App.instancesManager.planNewEval,100);
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
