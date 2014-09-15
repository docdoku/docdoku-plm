/*global define*/
define(['backbone'], function (Backbone) {

    var AttachedFile = Backbone.Model.extend({
        idAttribute: "fullName"
    });

    return AttachedFile;

});