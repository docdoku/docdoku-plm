define([
    'backbone',
    "common-objects/models/attribute"
], function (Backbone, AttributeModel) {

    var AttributeCollection = Backbone.Collection.extend({
        model: AttributeModel,
        className: "AttributeCollection"
    });

    return AttributeCollection;
});
