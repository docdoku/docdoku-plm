/*global define,_*/
define([
        'backbone',
        'text!templates/progress_bar.html'
    ],
    function (Backbone, template) {

        'use strict';

        var ProgressBar = Backbone.View.extend({

            tagName: 'div',
            el: '#progress_bar_container',

            initialize: function () {
                _.bindAll(this);
                this.total = 0;
                this.loaded = 0;
            },

            render: function () {
                this.$el.html(template);
                this.$bar = this.$('.bar');
                this.$bar.width(0);
                return this;
            },

            addTotal: function (total) {
                this.$bar.show();
                this.total += total;
            },

            removeXHRData: function (totalToRemove) {
                this.total -= totalToRemove;
                this.loaded -= totalToRemove;
                this.updateState();
            },

            addLoaded: function (loaded) {
                this.loaded += loaded;
                this.updateState();
            },

            percentValue: function () {
                return this.total > 0 ? (this.loaded / this.total) * 100 : 0;
            },

            updateState: function () {
                if (this.total) {
                    this.$bar.css('width', this.percentValue() + '%');
                } else {
                    this.hideProgressBar();
                }
            },

            hideProgressBar: function () {
                this.$bar.hide();
            }
        });

        return ProgressBar;
    }
);
