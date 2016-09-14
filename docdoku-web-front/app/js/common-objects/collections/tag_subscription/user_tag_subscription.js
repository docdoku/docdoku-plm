/*global define,App,$*/
define([
    'backbone',
    'common-objects/models/tag_subscription'
], function (Backbone, TagSubscription) {

    'use strict';

    var UserTagSubscriptions = Backbone.Collection.extend({
        model: TagSubscription,

        className: 'UserTagSubscriptions',

        url: function (user) {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/users/' + user + '/tag-subscriptions';
        }
    });

    return UserTagSubscriptions;
});
