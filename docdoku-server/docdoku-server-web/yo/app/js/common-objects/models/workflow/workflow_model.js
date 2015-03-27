/*global _,define,App,$*/
define([
    'backbone',
    'common-objects/collections/activity_models','common-objects/utils/acl-checker'
], function (Backbone, ActivityModels,ACLChecker) {
	'use strict';
    var WorkflowModel = Backbone.Model.extend({

        initialize: function () {
            _.bindAll(this);
        },
        urlRoot: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/workflows';
        },

        defaults: function () {
            return {
                activityModels: new ActivityModels()
            };
        },

        parse: function (response) {
            response.activityModels = new ActivityModels(response.activityModels);
            return response;
        },

        getId: function () {
            return this.get('id');
        },
        hasACLForCurrentUser: function () {
            return this.getACLPermissionForCurrentUser() !== false;
        },

        isForbidden: function () {
            return this.getACLPermissionForCurrentUser() === 'FORBIDDEN';
        },

        isReadOnly: function () {
            return this.getACLPermissionForCurrentUser() === 'READ_ONLY';
        },

        isFullAccess: function () {
            return this.getACLPermissionForCurrentUser() === 'FULL_ACCESS';
        },

        getACLPermissionForCurrentUser: function () {
            return ACLChecker.getPermission(this.get('acl'));
        },
        updateWorkflowACL: function (args) {
            return $.ajax({
                type: 'PUT',
                url: this.url()+'/acl',
                data: JSON.stringify(args.acl),
                contentType: 'application/json; charset=utf-8',
                success: args.success,
                error: args.error
            });
        }


    });
    return WorkflowModel;
});
