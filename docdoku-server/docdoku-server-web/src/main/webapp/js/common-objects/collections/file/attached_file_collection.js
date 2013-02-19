define([
    "common-objects/models/file/attached_file"
], function (
    AttachedFileModel
    ) {
    var AttachedFileCollection = Backbone.Collection.extend({
        model: AttachedFileModel,
        className : "AttachedFileCollection"
    });

    return AttachedFileCollection;
});
