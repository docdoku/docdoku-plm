/*global define*/
define([
    'backbone',
    "mustache",
    "common-objects/common/singleton_decorator",
    "text!templates/nav/part_template_nav.html",
    "views/part_template_content"
], function (Backbone, Mustache, singletonDecorator, template, PartTemplateContentView) {
    var PartTemplateNavView = Backbone.View.extend({

        el: "#part-template-nav",

        initialize: function () {
            this.render();
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n, workspaceId: App.config.workspaceId}));
        },

        setActive: function () {
            $("#product-management-menu").find(".active").removeClass("active");
            this.$el.find(".nav-list-entry").first().addClass("active");
        },

        showContent: function () {
            this.setActive();

            if (this.partTemplateContentView) {
                this.partTemplateContentView.undelegateEvents();
            }

            this.partTemplateContentView = new PartTemplateContentView().render();
        }

    });

    PartTemplateNavView = singletonDecorator(PartTemplateNavView);
    return PartTemplateNavView;

});
