/*global define*/
define([
    'backbone',
    "models/part_template"
], function (Backbone, Template) {
    var TemplateList = Backbone.Collection.extend({

        model: Template,

        url: function () {
            return App.config.contextPath + "/api/workspaces/" + App.config.workspaceId + "/part-templates";
        }

    });

    return TemplateList;
});
