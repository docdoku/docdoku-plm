/*global define*/
define([
        'backbone',
        "common-objects/common/singleton_decorator",
        "views/folder_nav",
        "views/tag_nav",
        "views/search_nav",
        "views/template_nav",
        "views/checkedout_nav",
        "views/task_nav"
    ],
    function (Backbone, singletonDecorator, FolderNavView, TagNavView, SearchNavView, TemplateNavView, CheckedoutNavView, TaskNavView) {
        var Router = Backbone.Router.extend({
            routes: {
                ":workspaceId/folders": "folders",
                ":workspaceId/folders/*path": "folder",
                ":workspaceId/tags": "tags",
                ":workspaceId/tags/:id": "tag",
                ":workspaceId/templates": "templates",
                ":workspaceId/checkedouts": "checkedouts",
                ":workspaceId/tasks": "tasks",
                ":workspaceId/tasks/:filter": "tasks",
                ":workspaceId/search/:query": "search",
                ":workspaceId": "defaults"
            },
            folders: function (workspaceId) {
                this.defaults(workspaceId);
                FolderNavView.getInstance().toggle();
            },
            folder: function (workspaceId, path) {
                this.defaults(workspaceId);
                FolderNavView.getInstance().show(decodeURIComponent(path));
            },
            tags: function (workspaceId) {
                this.defaults(workspaceId);
                TagNavView.getInstance().toggle();
            },
            tag: function (workspaceId, id) {
                this.defaults(workspaceId);
                TagNavView.getInstance().show(id);
            },
            templates: function (workspaceId) {
                this.defaults(workspaceId);
                var view = TemplateNavView.getInstance();
                view.showContent();
            },
            checkedouts: function (workspaceId) {
                this.defaults(workspaceId);
                CheckedoutNavView.getInstance().showContent();
            },
            tasks: function (workspaceId, filter) {
                this.defaults(workspaceId);
                TaskNavView.getInstance().showContent(filter);
            },
            search: function (workspaceId, query) {
                this.defaults(workspaceId);
                SearchNavView.getInstance().showContent(query);
            },
            defaults: function (workspaceId) {

                if (workspaceId != APP_CONFIG.workspaceId) {
                    location.reload();
                    return;
                }

                FolderNavView.getInstance();
                TagNavView.getInstance();
                TemplateNavView.getInstance();
                CheckedoutNavView.getInstance();
                SearchNavView.getInstance();
                TaskNavView.getInstance();
            }
        });


        return singletonDecorator(Router);
    });
