/*global define,App*/
define([
    'common-objects/common/singleton_decorator',
    'common-objects/views/base',
    'views/search_document_list',
    'views/advanced_search',
    'text!templates/search_nav.html'
], function (singletonDecorator, BaseView, SearchDocumentListView, AdvancedSearchView, template) {
	'use strict';
    var SearchNavView = BaseView.extend({
        template: template,

        el: '#search-nav',

        initialize: function () {
            BaseView.prototype.initialize.apply(this, arguments);
            this.events['click div'] = 'onClick';
            this.render();
        },

        setActive: function () {
            if (App.$documentManagementMenu) {
                App.$documentManagementMenu.find('.active').removeClass('active');
            }
            this.$el.find('.nav-list-entry').first().addClass('active');
        },

        showContent: function (query) {
            this.setActive();
            this.addSubView(
                new SearchDocumentListView({query: query})
            ).render();
        },

        onClick: function () {
            var advancedSearchView = new AdvancedSearchView();
            window.document.body.appendChild(advancedSearchView.render().el);
            advancedSearchView.openModal();
        }
    });
    SearchNavView = singletonDecorator(SearchNavView);
    return SearchNavView;
});
