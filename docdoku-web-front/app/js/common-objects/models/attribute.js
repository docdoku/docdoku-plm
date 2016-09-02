/*global define*/
define(['backbone'], function (Backbone) {
    'use strict';
    var Attribute = Backbone.Model.extend({

        getType: function () {
            if (this.get('type')) {
                return this.get('type');
            } else if (this.get('attributeType')) {
                return this.get('attributeType');
            } else {
                return null;
            }
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
        LONG_TEXT: 'LONG_TEXT',
        URL: 'URL',
        LOV: 'LOV'
    };

    return Attribute;
});
