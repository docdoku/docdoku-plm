define(function() {

    var ConfigurationItem = Backbone.Model.extend({

        urlRoot: '/api/workspaces/' + APP_CONFIG.workspaceId + '/products',

        idAttribute: "_id",

        parse: function(response) {
            response._id = response.id;
            return response;
        }

    });

    return ConfigurationItem;

});
