/*global define,App*/
define([
    'backbone',
    'common-objects/models/lov/lov'
], function (Backbone, lov) {
    'use strict';
    var LOVCollection = Backbone.Collection.extend({

        model: lov,

        url: App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/lov'

    });

    return LOVCollection;
});
