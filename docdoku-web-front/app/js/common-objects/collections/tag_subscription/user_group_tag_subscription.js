/*global define,App,$*/
define([
    'backbone',
    'common-objects/models/tag_subscription'
], function (Backbone, TagSubscription) {

    'use strict';

    var UserGroupTagSubscriptions = Backbone.Collection.extend({
        model: TagSubscription,

        className: 'UserGroupTagSubscriptions',

        url: function (group) {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/groups/' + group + '/tag-subscriptions';
        }
    });

    return UserGroupTagSubscriptions;
});
