define(["text!templates/content.html", "views/nav_view"], function(template, NavView) {

    var MainView =  Backbone.View.extend({

        template: Handlebars.compile(template),

        render:function() {
            this.$el.html(this.template({}));
            new NavView({el: "#nav"}).render();

            return this;
        }
    });

    return MainView;
});