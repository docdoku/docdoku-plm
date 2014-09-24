/*global define,App*/
define([
    'backbone',
    "mustache",
    "common-objects/common/singleton_decorator",
    "text!templates/nav/baselines_nav.html",
    "views/baseline/baselines_content"
], function (Backbone, Mustache, singletonDecorator, template, BaselinesContentView) {
    var BaselinesNavView = Backbone.View.extend({

        el: "#baselines-nav",

        initialize: function () {
            this.render();
            this.contentView = undefined;
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n, workspaceId: App.config.workspaceId}));
        },

        setActive: function () {
            if (App.$productManagementMenu) {
                App.$productManagementMenu.find(".active").removeClass("active");
            }
            this.$el.find(".nav-list-entry").first().addClass("active");
        },

        showContent: function (elementId) {
            this.setActive();
            this.clearView();
            this.contentView = new BaselinesContentView().render();
            $(elementId).html(this.contentView.el);
        },

        clearView: function () {
            if (this.contentView) {
                this.contentView.undelegateEvents();
                this.contentView.remove();
            }
        }

    });

    BaselinesNavView = singletonDecorator(BaselinesNavView);
    return BaselinesNavView;
});
