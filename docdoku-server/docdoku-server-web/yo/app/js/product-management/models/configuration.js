/*global _,$,define,App*/
define(['backbone'], function (Backbone) {
    'use strict';
    var Configuration = Backbone.Model.extend({
        urlRoot: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/configurations';
        },
        initialize: function () {
            _.bindAll(this);
        }
    });
    return Configuration;
});
