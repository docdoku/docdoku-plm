/*global define*/
define([
    'mustache',
    'common-objects/common/singleton_decorator',
    'views/folder_list_item',
    'text!templates/folder_nav.html'
], function (Mustache, singletonDecorator, FolderListItemView, template) {
    'use strict';
	var FolderNavView = FolderListItemView.extend({

        template: template,
        el: '#folder-nav',
        initialize: function () {
            FolderListItemView.prototype.initialize.apply(this, arguments);
            this.render();
        },
        refresh:function(){
            this.render();
        }
    });
    FolderNavView = singletonDecorator(FolderNavView);
    return FolderNavView;
});
