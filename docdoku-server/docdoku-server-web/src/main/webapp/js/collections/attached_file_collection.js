define([
    "models/attached_file"
], function (
    AttachedFileModel
    ) {
    var AttachedFileCollection = Backbone.Collection.extend({
        model: AttachedFileModel

    });
    AttachedFileCollection.className="AttachedFileCollection";
    return AttachedFileCollection;
});
