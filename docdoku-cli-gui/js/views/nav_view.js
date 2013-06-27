define(["text!templates/nav.html", "views/configuration_view"], function(template, ConfigView) {
    var NavView = Backbone.View.extend({

        template: Handlebars.compile(template),

        events: {
            "click .icon-cog" : "openConfigView"
        },

        render:function() {
            this.$el.html(this.template({}));

            return this;
        },

        openConfigView:function() {
            var configView = new ConfigView();
            $("body").append(configView.render().el);
            configView.openModal();
        }
    });

    return NavView;
});