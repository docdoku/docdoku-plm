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
                "room=:key":"joinCollaborative",
                //"share-view/:uuid":"enterCollaborativeModeAsSlave"
            },
            defaults: function() {

            },
            /*
            enterCollaborativeModeAsSlave:function(uuid){
                //App.sceneManager.handleCollaborative(uuid);
                App.appView.setSpectatorView();
            },*/
            joinCollaborative:function(key){
                App.sceneManager.joinRoom(key);

            }
        });
        Router = singletonDecorator(Router);
        return Router;
    });
