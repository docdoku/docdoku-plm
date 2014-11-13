/*global define,App*/
define([
    'backbone',
    'common-objects/models/role'
], function (Backbone, Role) {
	'use strict';
    var RoleInUseList = Backbone.Collection.extend({
        model: Role,

        url: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/roles/inuse';
        }

    });

    return RoleInUseList;
});
