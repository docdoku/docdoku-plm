define([
    "common/singleton_decorator",
    "views/base",
    "views/search_document_list",
    "text!templates/search_nav.html"
], function (
    singletonDecorator,
    BaseView,
    SearchDocumentListView,
    template
    ) {
    var SearchNavView = BaseView.extend({
        template: Mustache.compile(template),
        el: "#search-nav",
        initialize: function () {
            BaseView.prototype.initialize.apply(this, arguments);
            this.render();
        },
        setActive: function () {
            $("#nav .active").removeClass("active");
            this.$el.find(".nav-list-entry").first().addClass("active");
        },
        showContent: function (query) {
            this.setActive();
            this.addSubView(
                new SearchDocumentListView({query:query})
            ).render();
        }
    });
    SearchNavView = singletonDecorator(SearchNavView);
    return SearchNavView;
});
