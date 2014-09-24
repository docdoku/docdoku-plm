/*global define*/
define([
    'backbone',
    'common-objects/models/attribute'
], function (Backbone, AttributeModel) {
	'use strict';
    var AttributeCollection = Backbone.Collection.extend({
        model: AttributeModel,
        className: 'AttributeCollection'
    });

    return AttributeCollection;
});
