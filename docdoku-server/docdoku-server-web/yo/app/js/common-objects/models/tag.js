/*global define*/
define(['backbone'], function (Backbone) {
    var Tag = Backbone.Model.extend({
        initialize: function () {
            this.className = "Tag";
        }
    });
    return Tag;
});
