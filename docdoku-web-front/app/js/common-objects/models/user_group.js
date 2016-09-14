/*global define*/
define(['backbone'],
function (Backbone) {
	'use strict';
    var UserGroupModel = Backbone.Model.extend({
        getId: function () {
            return this.get('id');
        }
    });

    UserGroupModel.getTagSubscriptions = function (workspaceId, group) {
        return $.getJSON(App.config.contextPath + '/api/workspaces/' + workspaceId + '/groups/' + group + '/tag-subscriptions');
    };

    return UserGroupModel;
});
