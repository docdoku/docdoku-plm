/*global define,App*/
define([
    'models/change_item'
], function (ChangeItemModel) {
	'use strict';
    var ChangeIssueModel = ChangeItemModel.extend({
        urlRoot: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/changes/issues';
        },
        getInitiator: function () {
            return this.get('initiator');
        }
    });

    return ChangeIssueModel;
});