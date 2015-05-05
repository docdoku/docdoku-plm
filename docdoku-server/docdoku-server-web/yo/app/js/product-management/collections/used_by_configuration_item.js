/*global define,App*/
define([
    'backbone',
    'models/configuration_item'
], function (Backbone, ConfigurationItem) {
    'use strict';
    var UsedByConfigurationItemList = Backbone.Collection.extend({

        model: ConfigurationItem,

        setLinkedPart: function (linkedPart) {
            this.linkedPart = linkedPart;
        },

        url: function () {
            return this.linkedPart.url() + '/used-by-configurationItem';
        }

    });

    UsedByConfigurationItemList.className = 'UsedByConfigurationItemList';

    return UsedByConfigurationItemList;
});
