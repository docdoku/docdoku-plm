/*global define*/
define(['backbone'], function (Backbone) {
    var Folder = Backbone.Model.extend({

        initialize: function () {
            this.className = "Folder";
        },

        getPath: function () {
            return this.get("path");
        },

        getName: function () {
            return this.get("name");
        },

        defaults: {
            home: false
        },
        url: function () {
            if (this.get("id")) {
                return App.config.contextPath + "/api/workspaces/" + App.config.workspaceId + "/folders/" + this.get("id");
            } else if (this.collection) {
                return this.collection.url;
            }
        }
    });
    return Folder;
});
