/*global define,App,$*/
define([
    'backbone',
    'mustache',
    'common-objects/common/singleton_decorator',
    'text!templates/nav/checkedouts_nav.html',
    'views/part/part_content',
    'collections/checkedouts_part_collection'
], function (Backbone, Mustache, singletonDecorator, template, PartContentView,CheckedOutPartsCollection) {
	'use strict';
    var CheckedOutNavView = Backbone.View.extend({
        el: '#checkedout-nav',

        initialize: function () {
            this.render();
            Backbone.Events.on('part:iterationChange', this.refreshCount, this);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n, workspaceId: App.config.workspaceId}));
            this.refreshCount();
        },

        refreshCount:function(){
            var that = this;
            $.ajax({
                context :this,
                type:'GET',
                url:App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/countCheckedOut'
            }).success(function(data){
                var numberOfItem = data.count;
                var badge = that.$('.badge.nav-checkedOut-number-item');
                badge.addClass('badge-info');
                badge.html(numberOfItem);
                if(numberOfItem > 0){
                    badge.addClass('badge-success');
                    badge.removeClass('badge-info');
                } else{
                    badge.addClass('badge-info');
                    badge.removeClass('badge-success');
                }
            });
        },

        setActive: function () {
            if (App.$productManagementMenu) {
                App.$productManagementMenu.find('.active').removeClass('active');
            }
            this.$el.find('.nav-list-entry').first().addClass('active');
        },

        showContent: function () {
            this.setActive();
            this.cleanView();
            if(!this.partContentView){
                this.partContentView = new PartContentView();
            }
            this.partContentView.setCollection(new CheckedOutPartsCollection()).render();
            App.$productManagementContent.html(this.partContentView.el);
        },

        cleanView: function () {
            if (this.partContentView) {
                this.partContentView.undelegateEvents();
                App.$productManagementContent.html('');
            }
        }
    });

    CheckedOutNavView = singletonDecorator(CheckedOutNavView);
    return CheckedOutNavView;
});
