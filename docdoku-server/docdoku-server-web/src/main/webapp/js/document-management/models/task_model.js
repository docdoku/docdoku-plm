define(
    function () {
    var TaskModel = Backbone.Model.extend({

        defaults: {
            title: "Task",
            instructions: "attention!",
            duration: 25
        },

        toJSON: function() {
            var index = this.collection.indexOf(this);
            _.extend(this.attributes, {num: index});

            return _.clone(this.attributes);
        }

    });

    return TaskModel;
});
