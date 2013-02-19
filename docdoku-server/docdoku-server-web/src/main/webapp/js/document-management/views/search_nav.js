define([
    "common-objects/common/singleton_decorator",
    "common-objects/views/base",
    "views/search_document_list",
    "views/advanced_search",
    "text!templates/search_nav.html"
], function (
    singletonDecorator,
    BaseView,
    SearchDocumentListView,
    AdvancedSearchView,
    template
    ) {
    var SearchNavView = BaseView.extend({
        template: Mustache.compile(template),
        el: "#search-nav",

        initialize: function () {
            BaseView.prototype.initialize.apply(this, arguments);
            this.events['click div'] = "onClick";
            this.render();
        },

        setActive: function () {
            $("#document-menu .active").removeClass("active");
            this.$el.find(".nav-list-entry").first().addClass("active");
        },

        showContent: function (query) {
            this.setActive();
            this.addSubView(
                new SearchDocumentListView({query:query})
            ).render();
        },

        onClick:function(){
            var advancedSearchView = new AdvancedSearchView();
            $("body").append(advancedSearchView.render().el);
            advancedSearchView.openModal();
            advancedSearchView.setRouter(this.router);
        }
    });
    SearchNavView = singletonDecorator(SearchNavView);
    return SearchNavView;
});
