/*global define,App*/
define([
    'backbone',
    'common-objects/common/singleton_decorator'
],
function (Backbone, singletonDecorator) {
    'use strict';
    var Router = Backbone.Router.extend({
        routes: {
            '':   'login'
        },

        login:function(){
            App.appView.render();
            App.headerView.render();
	    }
    });

    return singletonDecorator(Router);
});
