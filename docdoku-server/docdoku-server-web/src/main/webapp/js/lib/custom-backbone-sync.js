var backboneSync = Backbone.sync;

Backbone.sync = function(method, model, options) {

    if (model.localStorage || ((model.collection) && model.collection.localStorage)) {
        LocalStorage.sync(method, model, options);
    } else {
        backboneSync(method, model, options);
    }

}