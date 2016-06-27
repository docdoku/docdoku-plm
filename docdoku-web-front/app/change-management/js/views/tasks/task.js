/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/tasks/task.html',
    'common-objects/views/workflow/lifecycle'
], function (Backbone, Mustache, template, LifecycleView) {
    'use strict';
    var TaskView = Backbone.View.extend({
        events: {},

        initialize: function () {

        },

        render: function (taskId) {
            var _this = this;
            $.getJSON(App.config.contextPath+'/api/workspaces/'+App.config.workspaceId+'/tasks/'+taskId)
                .then(function(task){
                    _this.task = task;
                    return  $.getJSON(App.config.contextPath+'/api/workspaces/'+App.config.workspaceId+'/workflow-instances/'+task.workflowId);
                })
                .then(function(workflow){

                    _this.$el.html(Mustache.render(template, {task:_this.task, i18n: App.config.i18n}));

                    _this.lifecycleView = new LifecycleView()
                        .setAbortedWorkflowsUrl(App.config.contextPath+'/api/workspaces/'+App.config.workspaceId+'/workflow-instances/'+_this.task.workflowId+'/aborted')
                        .setWorkflow(workflow)
                        .setEntityType(_this.task.holderType)
                        .render();

                    _this.lifecycleView.on('lifecycle:change', function () {
                        window.location.reload();
                    });

                    _this.$('.workflow-detail').append(_this.lifecycleView.$el);
                });


            return this;
        }

    });
    return TaskView;
});
