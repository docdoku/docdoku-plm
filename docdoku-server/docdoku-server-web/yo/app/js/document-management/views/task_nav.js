/*global define,App*/
define([
    'common-objects/common/singleton_decorator',
    'common-objects/views/base',
    'views/task_document_list',
    'text!templates/task_nav.html'
], function (singletonDecorator, BaseView, TaskDocumentListView, template) {

    'use strict';

    var TaskNavView = BaseView.extend({

        template: template,

        el: '#task-nav',

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

        showContent: function (filter) {
            this.setActive();
            this.addSubView(
                new TaskDocumentListView({filter: filter})
            ).render();
        }
    });

    TaskNavView = singletonDecorator(TaskNavView);
    return TaskNavView;
});
