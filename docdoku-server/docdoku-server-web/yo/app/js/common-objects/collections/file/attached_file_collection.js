/*global define*/
define([
    'backbone',
    "common-objects/models/file/attached_file"
], function (Backbone, AttachedFileModel) {
    var AttachedFileCollection = Backbone.Collection.extend({
        model: AttachedFileModel,
        className: "AttachedFileCollection"
    });

    return AttachedFileCollection;
});
