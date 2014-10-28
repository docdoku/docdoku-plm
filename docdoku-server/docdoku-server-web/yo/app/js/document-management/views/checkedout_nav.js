/*global define,App*/
define([
    'common-objects/common/singleton_decorator',
    'common-objects/views/base',
    'views/checked_out_document_list',
    'text!templates/checkedout_nav.html'
], function (singletonDecorator, BaseView, CheckedoutContentListView, template) {
    var CheckedOutNavView = BaseView.extend({

        template: template,
        el: '#checked-out-nav',
        initialize: function () {
            BaseView.prototype.initialize.apply(this, arguments);
            this.render();
        },
        setActive: function () {
            if (App.$documentManagementMenu) {
                App.$documentManagementMenu.find('.active').removeClass('active');
            }
            this.$el.find('.nav-list-entry').first().addClass('active');
        },
        showContent: function () {
            this.setActive();
            this.addSubView(
                new CheckedoutContentListView()
            ).render();
        }
    });
    CheckedOutNavView = singletonDecorator(CheckedOutNavView);
    return CheckedOutNavView;
});
