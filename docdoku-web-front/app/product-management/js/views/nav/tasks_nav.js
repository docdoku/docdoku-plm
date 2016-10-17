/*global define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/common/singleton_decorator',
    'text!templates/nav/tasks_nav.html',
    'views/part/part_content',
    'collections/tasks_part_collection'
], function (Backbone, Mustache, singletonDecorator, template, PartContentView, TaskPartsCollection) {

    'use strict';

    var TasksNavView = Backbone.View.extend({
        el: '#task-nav',

        initialize: function () {
            this.render();
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n, workspaceId: App.config.workspaceId}));
        },

        setActive: function () {
            if (App.$productManagementMenu) {
                App.$productManagementMenu.find('.active').removeClass('active');
            }
            this.$('.nav-list-entry').first().addClass('active');
        },

        showContent: function (filter) {
            this.setActive();
            this.cleanView();
            var filter = filter || 'all';
            this.partContentView = new PartContentView({filter: filter});
            var collection = new TaskPartsCollection();
            collection.setFilterStatus(filter);
            this.partContentView.setCollection(collection).render();
            App.$productManagementContent.html(this.partContentView.el);
        },

        cleanView: function () {
            if (this.partContentView) {
                this.partContentView.undelegateEvents();
                App.$productManagementContent.html('');
            }
        }
    });

    TasksNavView = singletonDecorator(TasksNavView);
    return TasksNavView;
});
