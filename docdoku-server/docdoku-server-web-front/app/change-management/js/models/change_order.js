/*global $,define,App*/
define([
    'models/change_item'
], function (ChangeItemModel) {
	'use strict';
    var ChangeOrderModel = ChangeItemModel.extend({
        urlRoot: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/changes/orders';
        },

        getMilestoneId: function () {
            return this.get('milestoneId');
        },

        getAddressedChangeRequests: function () {
            return this.get('addressedChangeRequests');
        },

        saveAffectedRequests: function (requests, callback) {
            $.ajax({
                context: this,
                type: 'PUT',
                url: this.url() + '/affected-requests',
                data: JSON.stringify({requests:requests}),
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

    return ChangeOrderModel;
});
