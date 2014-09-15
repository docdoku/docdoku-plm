/*global define*/
define([
    'backbone',
    "models/part_template"
], function (Backbone, Template) {
    var TemplateList = Backbone.Collection.extend({

        model: Template,

        url: function () {
            return APP_CONFIG.contextPath + "/api/workspaces/" + APP_CONFIG.workspaceId + "/part-templates";
        }

    });

    return TemplateList;
});
