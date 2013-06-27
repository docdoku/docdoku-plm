define(["text!templates/main.html", "views/menu_view", "views/content_view"], function (MainView, MenuView, ContentView) {

    var AppView = Backbone.View.extend({

        template:Handlebars.compile(MainView),

        render:function() {
            this.$el.html(this.template({}));
            this.initViews();

            return this;
        },

        initViews:function() {
            new MenuView({el: "#menu"}).render();
            new ContentView({el: "#content"}).render();
        }
    });

    $(function() {
        new AppView({el:"body"}).render();
    });
});