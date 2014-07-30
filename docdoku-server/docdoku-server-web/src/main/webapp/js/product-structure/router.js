/*global App*/
define([
    "common-objects/common/singleton_decorator"
],
    function (
        singletonDecorator
        ) {
        var Router = Backbone.Router.extend({
            routes: {
                "":	"defaults",
                "room=:key":"joinCollaborative"
            },
            defaults: function() {

            },
            joinCollaborative:function(key){
                if (!App.collaborativeView.isMaster) {
                    App.sceneManager.requestJoinRoom(key);
                }
            }
        });
        Router = singletonDecorator(Router);
        return Router;
    });
