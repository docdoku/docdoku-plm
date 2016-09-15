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

    UserGroupModel.editTagSubscription = function (workspaceId, group, tag) {
        return $.ajax({
            type: 'PUT',
            url: App.config.contextPath + '/api/workspaces/' + workspaceId + '/groups/' + group + '/tag-subscriptions/' + tag.tag,
            data: JSON.stringify(tag),
            contentType: 'application/json; charset=utf-8'
        });
    };

    UserGroupModel.removeTagSubscription = function (workspaceId, group, tag) {
        return $.ajax({
            type: 'DELETE',
            url: App.config.contextPath + '/api/workspaces/' + workspaceId + '/groups/' + group + '/tag-subscriptions/' + tag.tag
        });
    };

    return UserGroupModel;
});
