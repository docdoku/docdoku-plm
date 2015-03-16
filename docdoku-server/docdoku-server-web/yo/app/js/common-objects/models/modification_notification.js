/*global _,define,App,$*/
define([
    'backbone',
    'common-objects/utils/date'
], function (Backbone, Date) {
    'use strict';
    var ModificationNotification = Backbone.Model.extend({

        idAttribute: 'modificationNotification',

        initialize: function () {
            _.bindAll(this);
            this.className = 'ModificationNotification';
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
        }

    });

    return ModificationNotification;

});
