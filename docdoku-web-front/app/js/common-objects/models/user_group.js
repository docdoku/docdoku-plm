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

    UserGroupModel.addOrEditTagSubscription = function (workspaceId, group, tagSubscription) {
        return $.ajax({
            type: 'PUT',
            url: App.config.contextPath + '/api/workspaces/' + workspaceId + '/groups/' + group + '/tag-subscriptions/' + tagSubscription.getTag(),
            data: JSON.stringify(tagSubscription),
            contentType: 'application/json; charset=utf-8'
        });
    };

    UserGroupModel.removeTagSubscription = function (workspaceId, group, tagId) {
        return $.ajax({
            type: 'DELETE',
            url: App.config.contextPath + '/api/workspaces/' + workspaceId + '/groups/' + group + '/tag-subscriptions/' + tagId
        });
    };

    return UserGroupModel;
});
