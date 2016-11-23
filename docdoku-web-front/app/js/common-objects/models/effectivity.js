/*global _,$,define,App,window*/
define([
        'backbone'
    ],
    function (Backbone) {
        'use strict';

        var Effectivity = Backbone.Model.extend({
            effectivityTypes: [{id: 'SERIALNUMBERBASEDEFFECTIVITY', name: 'EFFECTIVITY_SERIAL_NUMBER'},
                                    {id: 'DATEBASEDEFFECTIVITY', name: 'EFFECTIVITY_DATE'},
                                    {id: 'LOTBASEDEFFECTIVITY', name: 'EFFECTIVITY_LOT'}],

            getEffectivityTypeByName: function(typeName) {
              return _.find(this.effectivityTypes, function(elt) {
                return elt.name = typeName;
              });
            },

            getEffectivityTypeById: function(typeId) {
              return _.find(this.effectivityTypes, function(elt) {
                return elt.id = typeId;
              });
            },

            getId: function() {
              return this.get('id');
            },

            getName: function() {
              return this.get('name');
            },

            getType: function() {
              return this.get('typeEffectivity');
            },

            getDescription: function() {
              return this.get('description');
            },

            getConfigurationItemKey: function() {
              return this.get('configurationItemKey');
            },

            getStartSerialNumber: function() {
              return this.get('startNumber');
            },

            getEndSerialNumber: function() {
              return this.get('endNumber');
            },

            getStartDate: function() {
              return this.get('startDate');
            },

            getEndDate: function() {
              return this.get('endDate');
            },

            getStartLotId: function() {
              return this.get('startLotId');
            },

            getEndLotId: function() {
              return this.get('endLotId');
            },

        });

        return Effectivity;
    });
