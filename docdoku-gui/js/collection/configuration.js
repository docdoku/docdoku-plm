define([
    "models/configuration"
], function (Configuration) {
        var ConfigurationList = Backbone.Collection.extend({
            model: Configuration,

            className:"ConfigurationList",

            initialize:function() {}
    });

    return ConfigurationList;
});