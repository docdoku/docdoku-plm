define([
    "common-objects/common/singleton_decorator",
    "text!templates/part_nav.html",
    "i18n!localization/nls/product-management-strings",
    "views/part_content"
], function (
    singletonDecorator,
    template,
    i18n,
    PartContentView
    ) {
    var PartNavView = Backbone.View.extend({

        template: Mustache.compile(template),

        el: "#part-nav",

        initialize: function () {
            this.render();
        },

        render:function(){
            this.$el.html(this.template({i18n:i18n}));
        },

        setActive: function () {
            $("#product-management-menu .active").removeClass("active");
            this.$el.find(".nav-list-entry").first().addClass("active");
        },

        showContent: function (query) {
            this.setActive();

            if(this.partContentView){
                this.partContentView.undelegateEvents();
            }

            this.partContentView = new PartContentView().setQuery(query).render();

        }

    });

    PartNavView = singletonDecorator(PartNavView);
    return PartNavView;

});
