/*global define*/
define(['backbone', "common-objects/models/configuration_item" ], function (Backbone, ConfigurationItem) {

    var ConfigurationItemCollection = Backbone.Collection.extend({
        url: function () {
            return APP_CONFIG.contextPath + '/api/workspaces/' + APP_CONFIG.workspaceId + '/products';
        },
        model: ConfigurationItem
    });

    return ConfigurationItemCollection;

});
