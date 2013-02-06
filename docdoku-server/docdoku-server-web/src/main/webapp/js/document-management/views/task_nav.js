define([
    "common/singleton_decorator",
    "views/base",
    "views/task_document_list",
    "text!templates/task_nav.html"
], function (
    singletonDecorator,
    BaseView,
    TaskDocumentListView,
    template
    ) {
    var TaskNavView = BaseView.extend({
        template: Mustache.compile(template),
        el: "#task-nav",

        initialize: function () {
            BaseView.prototype.initialize.apply(this, arguments);
            this.render();
        },

        setActive: function () {
            $("#document-menu .active").removeClass("active");
            this.$el.find(".nav-list-entry").first().addClass("active");
        },

        showContent: function (query) {
            this.setActive();
            this.addSubView(
                new TaskDocumentListView()
            ).render();
        }
    });

    TaskNavView = singletonDecorator(TaskNavView);
    return TaskNavView;
});
