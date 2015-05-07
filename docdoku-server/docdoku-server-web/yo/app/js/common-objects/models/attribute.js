/*global define*/
define(['backbone'], function (Backbone) {
    'use strict';
    var Attribute = Backbone.Model.extend({

        getType: function () {
            return this.get('type');
        },

        isMandatory: function () {
            return this.get('mandatory');
        },

        getName: function () {
            return this.get('name');
        },

        getLOVName: function () {
            return this.get('lovName');
        },

        getValue: function () {
            return this.get('value');
        },

        getItems: function () {
            return this.get('items');
        },

        getLocked: function () {
            return this.get('locked');
        },

        toString: function () {
            return this.getName() + ':' + this.getValue() + '(' + this.getType() + ') ';
        }

    });

    Attribute.types = {
        NUMBER: 'NUMBER',
        DATE: 'DATE',
        BOOLEAN: 'BOOLEAN',
        TEXT: 'TEXT',
        URL: 'URL',
        LOV: 'LOV'
    };

    return Attribute;
});
