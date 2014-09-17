/*global define,App*/
define(['backbone', 'common-objects/models/configuration_item' ],
function (Backbone, ConfigurationItem) {
	'use strict';
    var ConfigurationItemCollection = Backbone.Collection.extend({
        url: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products';
        },
        model: ConfigurationItem
    });

    return ConfigurationItemCollection;

});
