define([
    "common-objects/common/singleton_decorator",
],
    function (
        singletonDecorator,
        controlModeView
        ) {
        var Router = Backbone.Router.extend({
            routes: {
                "":	"defaults",
                "share-view/:uuid":"enterCollaborativeModeAsSlave"
            },
            defaults: function() {

            },
            enterCollaborativeModeAsSlave:function(uuid){
                //App.sceneManager.handleCollaborative(uuid);
                App.appView.collaborativeMode();
            }
        });
        Router = singletonDecorator(Router);
        return Router;
    });
