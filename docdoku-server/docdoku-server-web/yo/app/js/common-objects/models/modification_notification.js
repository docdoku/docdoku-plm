/*global _,define,App,$*/
define([
    'backbone',
    'common-objects/utils/date'
], function (Backbone, date) {
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

        getCheckInDate: function () {
            return this.get('checkInDate');
        },

        getFormattedCheckInDate: function () {
            var checkInDate = this.getCheckInDate();
            if (checkInDate) {
                return date.formatTimestamp(
                    App.config.i18n._DATE_FORMAT,
                    checkInDate
                );
            }
            return null;
        },

        getIterationNote: function () {
            return this.get('iterationNote');
        },

        getAuthor: function () {
            return this.get('author');
        },

        getAuthorName: function () {
            return this.getAuthor().name;
        }

    });

    return ModificationNotification;

});
