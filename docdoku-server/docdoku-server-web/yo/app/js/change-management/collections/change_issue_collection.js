/*global define,App*/
define([
    'backbone',
    'models/change_issue'
], function (Backbone,ChangeIssueModel) {
	'use strict';
    var ChangeIssueListCollection = Backbone.Collection.extend({
        model: ChangeIssueModel,
        url: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/changes/issues';
        }
    });

    return ChangeIssueListCollection;
});