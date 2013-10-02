define(["text!templates/local_repository.html"], function(template) {
    var LocalRepositoryView = Backbone.View.extend({

        el: 'div#subContent',

        template: Handlebars.compile(template),

        events: {
        },

        render:function() {
            this.$el.html(this.template({}));

            return this;
        }
    });

    return LocalRepositoryView;
});