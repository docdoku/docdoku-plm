/*global define,App*/
define([
    'backbone',
    'models/template',
    'common-objects/common/singleton_decorator'
], function (Backbone, Template, singletonDecorator) {
    'use strict';
    var TemplateList = Backbone.Collection.extend({
        url: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/document-templates';
        },
        model: Template
    });

    TemplateList = singletonDecorator(TemplateList);
    TemplateList.className = 'TemplateList';

    return TemplateList;
});
