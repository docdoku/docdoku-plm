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
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            return this;
        }

    });
    return TaskListView;
});
