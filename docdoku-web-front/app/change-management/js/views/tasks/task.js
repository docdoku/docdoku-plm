/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/tasks/task.html',
    'common-objects/views/workflow/lifecycle'
], function (Backbone, Mustache, template, LifecycleView) {
    'use strict';
    var TaskView = Backbone.View.extend({

        initialize: function () {

        },

        renderTask: function (taskId) {
            var _this = this;
            $.getJSON(App.config.contextPath+'/api/workspaces/'+App.config.workspaceId+'/tasks/'+taskId)
                .then(function(task){
                    _this.task = task;
                    _this.renderWorkflow(task.workflowId);
                });
            return this;
        },

        renderWorkflow:function(workflowId){
            var _this = this;
            $.getJSON(App.config.contextPath+'/api/workspaces/'+App.config.workspaceId+'/workflow-instances/'+workflowId)
                .then(function(workflow){
                    _this.workflow = workflow;
                    _this.render();
                });
        },

        render:function(){
            this.$el.html(Mustache.render(template, {task:this.task, workflow:this.workflow, i18n: App.config.i18n}));

            this.lifecycleView = new LifecycleView()
                .setWorkflow(this.workflow)
                .render();

            var _this = this;
            this.lifecycleView.on('lifecycle:change', function () {
                _this.lifecycleView.displayWorkflow(_this.workflow);
                _this.renderWorkflow(_this.workflow.id);
            });

            this.$('.workflow-detail').append(this.lifecycleView.$el);
        }

    });
    return TaskView;
});
