/*global define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/common/singleton_decorator',
    'text!templates/nav/change_request_nav.html',
    'views/change-requests/change_request_content'
], function (Backbone, Mustache, singletonDecorator, template, ChangeRequestContentView) {
	'use strict';
    var ChangeRequestNavView = Backbone.View.extend({
        el: '#request-nav',

        initialize: function () {
            this.render();
            this.contentView = undefined;
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n, workspaceId: App.config.workspaceId}));
        },

        setActive: function () {
            if (App.$changeManagementMenu) {
                App.$changeManagementMenu.find('.active').removeClass('active');
            }
            this.$el.find('.nav-list-entry').first().addClass('active');
        },

        showContent: function () {
            this.setActive();
            this.cleanView();
            this.contentView = new ChangeRequestContentView().render();
	        App.appView.$content.html(this.contentView.el);
        },

        cleanView: function () {
            if (this.contentView) {
                this.contentView.remove();
            }
        }
    });

    var ChangeRequestView = singletonDecorator(ChangeRequestNavView);
    return ChangeRequestView;
});
