/*global APP_VIEW*/
define([
	"common-objects/common/singleton_decorator",
    "text!templates/nav/change_issue_nav.html",
    "i18n!localization/nls/change-management-strings",
	"views/change-issues/change_issue_content"
], function (
	singletonDecorator,
    template,
    i18n,
    ChangeIssueContentView
) {
	var ChangeIssueNavView = Backbone.View.extend({

        template: Mustache.compile(template),

        el: "#issue-nav",

        initialize: function () {
			this.render();
            this.contentView = undefined;
		},

        render:function(){
            this.$el.html(this.template({i18n:i18n}));
        },

        setActive: function () {
            if(APP_VIEW.$changeManagementMenu){
                APP_VIEW.$changeManagementMenu.find(".active").removeClass("active");
            }
			this.$el.find(".nav-list-entry").first().addClass("active");
		},

		showContent: function (elementId) {
			this.setActive();
            this.cleanView();
            this.contentView = new ChangeIssueContentView().render();
            $(elementId).html(this.contentView.el);
		},

        cleanView: function(){
            if(this.contentView){
                this.contentView.remove();
            }
        }
	});

    ChangeIssueNavView = singletonDecorator(ChangeIssueNavView);
	return ChangeIssueNavView;
});