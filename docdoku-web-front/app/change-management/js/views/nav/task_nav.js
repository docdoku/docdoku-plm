/*global define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/common/singleton_decorator',
    'views/tasks/task_content_list',
    'views/tasks/task'
], function (Backbone, Mustache, singletonDecorator, TaskListView, TaskView) {
	'use strict';
    var TaskNavView = Backbone.View.extend({

        initialize: function () {
            this.contentView = undefined;
        },

        render: function () {
        },

        setActive: function () {
            if (App.$changeManagementMenu) {
                App.$changeManagementMenu.find('.active').removeClass('active');
            }
        },

        showTaskContent: function (taskId) {
            this.setActive();
            this.cleanView();
            this.contentView = new TaskView();
            this.contentView.renderTask(taskId);
            App.appView.$content.html(this.contentView.el);
        },

        showWorkflowContent: function (workflowId) {
            this.setActive();
            this.cleanView();
            this.contentView = new TaskView();
            this.contentView.renderWorkflow(workflowId);
            App.appView.$content.html(this.contentView.el);
        },
        cleanView: function () {
            if (this.contentView) {
                this.contentView.remove();
            }
        }
    });

    TaskNavView = singletonDecorator(TaskNavView);
    return TaskNavView;
});
