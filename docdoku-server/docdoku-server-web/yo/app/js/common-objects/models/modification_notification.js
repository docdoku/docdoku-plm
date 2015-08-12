/*global _,define,App,$*/
define([
    'backbone',
    'common-objects/utils/date'
], function (Backbone, date) {
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
            return date.formatTimestamp(
                App.config.i18n._DATE_FORMAT,
                this.getCheckInDate()
            );
        },

        hasAuthor: function () {
            return !_.isNull(this.get('author'));
        },

        getAuthor: function () {
            return this.get('author');
        },

        getAuthorName: function () {
            return this.getAuthor().name;
        },

        getIterationNote: function () {
            return this.get('iterationNote');
        },

        isAcknowledged: function () {
            return this.get('acknowledged');
        },

        getAckComment: function () {
            return this.get('ackComment');
        },

        getAckAuthor: function () {
            return this.get('ackAuthor');
        },

        getAckAuthorName: function () {
            if (this.getAckAuthor()) {
                return this.getAckAuthor().name;
            } else {
                return App.config.userName;
            }
        },

        getAckDate: function () {
            return this.get('ackDate');
        },

        getFormattedAckDate: function () {
            return date.formatTimestamp(
                App.config.i18n._DATE_FORMAT,
                this.getAckDate()
            );
        },

        setAcknowledged: function (data) {
            return $.ajax({
                context: this,
                type: 'PUT',
                url: this.collection.url() + this.getId(),
                data: JSON.stringify(data),
                contentType: 'application/json; charset=utf-8',
                error: function (xhr) {
                    window.alert(xhr.responseText);
                    this.set('acknowledged', false);
                }
            }).success(function () {
                this.set('ackComment', data.ackComment);
                var now = new Date();
                var nowUtc = new Date(now.getUTCFullYear(),now.getUTCMonth(), now.getUTCDate() ,
                    now.getUTCHours(), now.getUTCMinutes(), now.getUTCSeconds());
                this.set('ackDate', (nowUtc).toString());
                this.set('acknowledged', true);
            });
        }

    });

    return ModificationNotification;

});
