/*global APP_VIEW*/
define([
    "common-objects/common/singleton_decorator",
    "text!templates/nav/baselines_nav.html",
    "i18n!localization/nls/baseline-strings",
    "views/baseline/baselines_content"
], function (
    singletonDecorator,
    template,
    i18n,
    BaselinesContentView
    ) {
    var BaselinesNavView = Backbone.View.extend({

        template: Mustache.compile(template),

        el: "#baselines-nav",

        initialize: function () {
            this.render();
            this.contentView = undefined;
        },

        render:function(){
            this.$el.html(this.template({i18n:i18n}));
        },

        setActive: function () {
            if(APP_VIEW.$productManagementMenu){
                APP_VIEW.$productManagementMenu.find(".active").removeClass("active");
            }
            this.$el.find(".nav-list-entry").first().addClass("active");
        },

        showContent: function (elementId) {
            this.setActive();
            this.clearView();
            this.contentView = new BaselinesContentView().render();
            $(elementId).html(this.contentView.el);
        },

        clearView: function(){
            if(this.contentView){
                this.contentView.undelegateEvents();
                this.contentView.remove();
            }
        }

    });

    BaselinesNavView = singletonDecorator(BaselinesNavView);
    return BaselinesNavView;
});
