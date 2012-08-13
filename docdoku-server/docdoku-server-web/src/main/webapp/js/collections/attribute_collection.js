define([
    "models/attribute"
], function (AttributeModel) {

    var AttributeCollection = Backbone.Collection.extend({
        model: AttributeModel
    });

    AttributeCollection.className="AttributeCollection";
    return AttributeCollection;
});
