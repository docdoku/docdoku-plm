/*global $,define,App*/
define([
    'models/change_item'
], function (ChangeItemModel) {
	'use strict';
    var ChangeRequestModel = ChangeItemModel.extend({
        urlRoot: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/changes/requests';
        },
        getMilestoneId: function () {
            return this.get('milestoneId');
        },

        getAddressedChangeIssues: function () {
            return this.get('addressedChangeIssues');
        },

        saveAffectedIssues: function (issues, callback) {
            $.ajax({
                context: this,
                type: 'PUT',
                url: this.url() + '/affected-issues',
                data: JSON.stringify({issues:issues}),
                contentType: 'application/json; charset=utf-8',
                success: function () {
                    this.fetch();
                    if (callback) {
                        callback();
                    }
                }
            });
        }
    });

    return ChangeRequestModel;
});
