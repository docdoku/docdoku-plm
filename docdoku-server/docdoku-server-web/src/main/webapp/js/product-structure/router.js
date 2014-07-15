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
                App.sceneManager.joinRoom(key);

            }
        });
        Router = singletonDecorator(Router);
        return Router;
    });
