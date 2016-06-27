/*global define,App*/
define([
    'backbone',
    'common-objects/common/singleton_decorator'
],
function (Backbone, singletonDecorator) {
    'use strict';
    var Router = Backbone.Router.extend({
        routes: {
            '':   'editAccount'
        },

        editAccount:function(){
            App.appView.render();
            App.headerView.render();
            App.appView.editAccount();
	    }
    });

    return singletonDecorator(Router);
});
