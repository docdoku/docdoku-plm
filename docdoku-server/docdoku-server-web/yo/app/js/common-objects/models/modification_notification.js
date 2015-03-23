/*global _,define,App,$*/
define([
    'backbone',
    'common-objects/utils/date'
], function (Backbone, Date) {
    'use strict';
    var ModificationNotification = Backbone.Model.extend({

        idAttribute: 'id',

        initialize: function () {
            _.bindAll(this);
            this.className = 'ModificationNotification';
        },

        getId: function () {
            return this.get('id');
        },

        getImpactedPartNumber: function () {
            return this.get('impactedPartNumber');
        },

        getImpactedPartVersion: function () {
            return this.get('impactedPartVersion');
        },

        getModifiedPartNumber: function () {
            return this.get('modifiedPartNumber');
        },

        getModifiedPartVersion: function () {
            return this.get('modifiedPartVersion');
        },

        getModifiedPartIteration: function () {
            return this.get('modifiedPartIteration');
        },

        getModifiedPartName: function () {
            return this.get('modifiedPartName');
        },

        hasCheckInDate: function () {
            return !_.isNull(this.get('checkInDate'));
        },

        getCheckInDate: function () {
            return this.get('checkInDate');
        },

        getFormattedCheckInDate: function () {
            if (this.hasCheckInDate()) {
                return Date.formatTimestamp(
                    App.config.i18n._DATE_FORMAT,
                    this.getCheckInDate()
                );
            }
            return null;
        },

        hasAuthor: function () {
            return !_.isNull(this.get('author'));
        },

        getAuthor: function () {
            return this.get('author');
        },

        getAuthorName: function () {
            if (this.hasAuthor()) {
                return this.getAuthor().name;
            }
            return null;
        },

        getIterationNote: function () {
            return this.get('iterationNote');
        },

        isAcknowledged: function () {
            return this.get('acknowledged');
        },

        setAcknowledged: function (callback) {
            return $.ajax({
                context: this,
                type: 'PUT',
                url: this.url() + '/modificationNotifications/' + this.getId() + '/acknowledge',
                error: function (xhr) {
                    window.alert(xhr.responseText);
                }
            }).success(callback());
        },

        url: function () {
            var url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/';
            if (this.getImpactedPartNumber() && this.getImpactedPartVersion()) {
                return url + this.getImpactedPartNumber() + '-' + this.getImpactedPartVersion();
            }
            return url;
        }

    });

    return ModificationNotification;

});
