/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/control_measure.html'
],
function (Backbone, Mustache, template) {
	'use strict';
    var MeasureOptionsView = Backbone.View.extend({

        className: 'side_control_group',

        events: {
            'click .clear-measures-btn':'onClearClicked'
        },

        initialize: function () {
            _.bindAll(this);
            this.state = false;
            Backbone.Events.on('measure:drawn', this.displayClearButton);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.$switch = this.$('.measure-switch');
            this.$switch.bootstrapSwitch();
            this.$switch.bootstrapSwitch('setState', this.state);
            this.$switch.on('switch-change', this.switchMeasureState);
            return this;
        },

        switchMeasureState: function (e, data) {
            this.state = data.value;
            App.sceneManager.setMeasureState(data.value);
        },

        onClearClicked: function(/*e*/){
            App.sceneManager.clearMeasures();
            this.$('.clear-measures-btn').removeClass('display');
        },

        displayClearButton: function(){
            this.$('.clear-measures-btn').addClass('display');
        }

    });

    return MeasureOptionsView;

});
