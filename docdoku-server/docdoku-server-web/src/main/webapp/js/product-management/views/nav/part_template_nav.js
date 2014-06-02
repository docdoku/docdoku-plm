define([
    "common-objects/common/singleton_decorator",
    "text!templates/nav/part_template_nav.html",
    "i18n!localization/nls/product-management-strings",
    "views/part_template_content"
], function (
    singletonDecorator,
    template,
    i18n,
    PartTemplateContentView
    ) {
    var PartTemplateNavView = Backbone.View.extend({

        template: Mustache.compile(template),

        el: "#part-template-nav",

        initialize: function () {
            this.render();
        },

        render:function(){
            this.$el.html(this.template({i18n:i18n}));
        },

        setActive: function () {
            $("#product-management-menu").find(".active").removeClass("active");
            this.$el.find(".nav-list-entry").first().addClass("active");
        },

        showContent: function () {
            this.setActive();

            if(this.partTemplateContentView){
                this.partTemplateContentView.undelegateEvents();
            }

            this.partTemplateContentView = new PartTemplateContentView().render();
        }

    });

    PartTemplateNavView = singletonDecorator(PartTemplateNavView);
    return PartTemplateNavView;

});
