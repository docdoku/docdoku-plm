/*global define,App*/
define([
    'backbone',
    'common-objects/models/user_group'
], function (Backbone, UserGroup) {
	'use strict';
    var UserGroups = Backbone.Collection.extend({
        model: UserGroup,
        url: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/user-group';
        }
    });

    return UserGroups;
});
