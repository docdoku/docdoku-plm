define([ "modules/product-creation-module/models/configuration_item" ],function(ConfigurationItem) {

    var ConfigurationItemCollection = Backbone.Collection.extend({
        url: '/api/workspaces/' + APP_CONFIG.workspaceId + '/products',
        model: ConfigurationItem
    });

    return ConfigurationItemCollection;

});
