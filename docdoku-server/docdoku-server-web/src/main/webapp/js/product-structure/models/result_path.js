define(function () {

    var ResultPath = Backbone.Model.extend({

        parse: function(response) {
            if (response) {
                this.path = response;
            }
        },

        toJSON: function() {
            return _.pick(this.attributes, 'path');
        }

    });

    return ResultPath;

});
