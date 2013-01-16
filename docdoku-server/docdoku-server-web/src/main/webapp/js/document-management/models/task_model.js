define([
    "common-objects/models/user"
],function (
    User
) {
    var TaskModel = Backbone.Model.extend({

        defaults: {
            duration: 25
        },

        initialize: function() {
            if(!_.isUndefined(this.attributes.worker))
                this.attributes.worker = new User(this.attributes.worker);
        },

        toJSON: function() {
            var index = this.collection.indexOf(this);
            _.extend(this.attributes, {num: index});

            return _.clone(this.attributes);
        }

    });

    return TaskModel;
});
