/*global define,App,$*/
define([
    'common-objects/common/singleton_decorator',
    'common-objects/views/base',
    'views/checked_out_document_list',
    'text!templates/checkedout_nav.html',
    'backbone'
], function (singletonDecorator, BaseView, CheckedoutContentListView, template, Backbone) {

    'use strict';

    var CheckedOutNavView = BaseView.extend({

        template: template,
        el: '#checked-out-nav',
        initialize: function () {
            BaseView.prototype.initialize.apply(this, arguments);
            Backbone.Events.on('document:iterationChange', this.refreshCount, this);
            this.render();
        },

        rendered:function(){
            this.refreshCount();
        },

        refreshCount:function(){
            var that = this;
            $.ajax({
                context :this,
                type:'GET',
                url:App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/documents/countCheckedOut'
            }).success(function(data){
                var numberOfItem = data.count;
                var badge = that.$('.badge.nav-checkedOut-number-item');
                badge.html(numberOfItem);
               /* if(numberOfItem === 0){
                    badge.addClass('badge-success');
                    badge.removeClass('badge-info');
                } else{
                    badge.addClass('badge-info');
                    badge.removeClass('badge-success');
                }*/
            });
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
