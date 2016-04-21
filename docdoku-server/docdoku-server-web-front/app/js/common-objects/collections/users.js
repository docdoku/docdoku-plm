/*global define,App*/
define([
    'backbone',
    'common-objects/models/user'
], function (Backbone, User) {
	'use strict';
    var Users = Backbone.Collection.extend({
        model: User,
        url: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/users';
        }
    });

    return Users;
});
