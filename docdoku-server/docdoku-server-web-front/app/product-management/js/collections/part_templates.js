/*global define,App*/
define([
    'backbone',
    'models/part_template'
], function (Backbone, Template) {
    'use strict';
    var TemplateList = Backbone.Collection.extend({
        model: Template,

        url: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/part-templates';
        }

    });

    return TemplateList;
});
