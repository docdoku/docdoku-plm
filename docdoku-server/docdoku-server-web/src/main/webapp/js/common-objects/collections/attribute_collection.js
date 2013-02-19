define([
    "common-objects/models/attribute"
], function (AttributeModel) {

    var AttributeCollection = Backbone.Collection.extend({
        model: AttributeModel,
        className:"AttributeCollection"
    });

    return AttributeCollection;
});
