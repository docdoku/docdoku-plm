define([
    "common-objects/common/singleton_decorator"
],
    function (
        singletonDecorator
        ) {
        var Router = Backbone.Router.extend({
            routes: {
                "":	"defaults"
            },
            defaults: function() {

            }
        });
        Router = singletonDecorator(Router);
        return Router;
    });
