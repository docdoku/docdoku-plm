/*global define,App*/
define([
    'common-objects/common/singleton_decorator',
    'common-objects/views/base',
    'views/template_content_list',
    'text!templates/template_nav.html'
], function (singletonDecorator, BaseView, TemplateContentListView, template) {

    'use strict';

    var TemplateNavView = BaseView.extend({

        template: template,
        el: '#template-nav',

        initialize: function () {
            BaseView.prototype.initialize.apply(this, arguments);
            this.render();
        },
        setActive: function () {
            if (App.$documentManagementMenu) {
                App.$documentManagementMenu.find('.active').removeClass('active');
            }
            this.$('.nav-list-entry').first().addClass('active');
        },
        showContent: function () {
            this.setActive();
            this.addSubView(
                new TemplateContentListView()
            ).render();
        }
    });
    TemplateNavView = singletonDecorator(TemplateNavView);
    return TemplateNavView;
});
