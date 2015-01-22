/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/time_zone.html',
    'moment',
    'common-objects/utils/date'
], function (Backbone, Mustache, template, moment, date) {
    'use strict';
    var TimeZoneView = Backbone.View.extend({
        events: {
            'hidden #exportSceneModal': 'onHidden'
        },

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                dates:this.dates
            }));
            this.$modal = this.$('#timezone_modal');
            return this;
        },

        setTimestamp:function(timestamp){
            this.dates = date.getMainZonesDates(timestamp);
            return this;
        },

        openModal: function () {
            this.$modal.modal('show');
        },

        closeModal: function () {
            this.$modal.modal('hide');
        },

        onHidden: function () {
            this.remove();
        }


    });

    return TimeZoneView;
});
