/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/part/cad_instance.html'
], function (Backbone, Mustache, template) {
	'use strict';
    var CadInstanceView = Backbone.View.extend({

        className: 'cad-instance',

        events: {
            'change input[name=tx]': 'changeTX',
            'change input[name=ty]': 'changeTY',
            'change input[name=tz]': 'changeTZ',
            'change input[name=rx]': 'changeRX',
            'change input[name=ry]': 'changeRY',
            'change input[name=rz]': 'changeRZ',
            'change input':'onChange',
            'click .delete-cad-instance': 'removeCadInstance'
        },

        initialize: function () {
        },

        render: function () {
            var disabled = this.options.editMode ? '':'disabled';
            this.$el.html(Mustache.render(template, {
                canRemove:this.options.editMode,
                disabled:disabled,
                instance: this.model.attributes,
                i18n: App.config.i18n
            }));
            return this;
        },

        changeTX: function (e) {
            this.model.set('tx', e.target.value);
        },

        changeTY: function (e) {
            this.model.set('ty',e.target.value);
        },

        changeTZ: function (e) {
            this.model.set('tz', e.target.value);
        },

        changeRX: function (e) {
            this.model.set('rx', e.target.value);
        },

        changeRY: function (e) {
            this.model.set('ry', e.target.value);
        },

        changeRZ: function (e) {
            this.model.set('rz', e.target.value);
        },

        removeCadInstance: function () {
            this.model.collection.remove(this.model);
            this.remove();
        }

    });

    return CadInstanceView;
});
