/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/tasks/task_list.html'
], function (Backbone, Mustache, template) {
    'use strict';
    var TaskListView = Backbone.View.extend({
        events: {},

        initialize: function () {

        },

        render: function () {
            var _this = this;
            $.getJSON(App.config.contextPath+'/api/workspaces/'+App.config.workspaceId+'/tasks/'+App.config.login+'/assigned')
                .then(function(tasks){
                    _this.$el.html(Mustache.render(template, {tasks:tasks,i18n: App.config.i18n}));
                });
            return this;
        }

    });
    return TaskListView;
});
