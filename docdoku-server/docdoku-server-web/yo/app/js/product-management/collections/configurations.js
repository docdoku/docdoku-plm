/*global define,App*/
define(['backbone', 'models/configuration' ],
function (Backbone, Configuration) {
	'use strict';
    var ConfigurationCollection = Backbone.Collection.extend({
        url: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/configurations';
        },
        model: Configuration
    });

    return ConfigurationCollection;

});
