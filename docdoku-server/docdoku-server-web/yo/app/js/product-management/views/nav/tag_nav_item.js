/*global define,App,bootbox*/
define([
    'backbone',
    'mustache',
    'text!templates/nav/tag_nav_item.html',
    'common-objects/models/part'
], function (Backbone, Mustache, template, Part) {
	'use strict';
    var TagNavItemView = Backbone.View.extend({
        events:{
            'click .delete': 'actionDelete',
            'mouseleave .header': 'hideActions',
            'dragenter >.nav-list-entry': 'onDragEnter',
            'dragover >.nav-list-entry': 'checkDrag',
            'dragleave >.nav-list-entry': 'onDragLeave',
            'drop >.nav-list-entry': 'onDrop'
        },
        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                workspaceId: App.config.workspaceId,
                model:this.model
            }));
            this.tagDiv = this.$('>.nav-list-entry');
            return this;
        },
        hideActions:function() {
            this.$('.header .btn-group').first().removeClass('open');
        },
        actionDelete:function() {
            this.hideActions();
            var that = this ;
            bootbox.confirm(App.config.i18n.DELETE_TAG_QUESTION, function(result){
                if(result){
                    that.model.destroy({
                        dataType: 'text' // server doesn't send a json hash in the response body
                    }).success(that.remove.bind(that));
                }
            });
            return false;
        },

        onDragEnter: function () {
            this.tagDiv.hasClass('move-part-into');
        },

        checkDrag: function (e) {
            e.dataTransfer.dropEffect = 'copy';
            this.tagDiv.addClass('move-part-into');
            return false;
        },

        onDragLeave: function (e) {
            e.dataTransfer.dropEffect = 'none';
            this.tagDiv.removeClass('move-part-into');
        },

        onDrop: function (e) {
            if (e.dataTransfer.getData('part:text/plain')) {
                this.tagPart(e);
            }
        },

        tagPart: function(e) {
            var that = this;
            var part = new Part(JSON.parse(e.dataTransfer.getData('part:text/plain')));

            part.addTags([this.model]).success(function () {
                that.tagDiv.removeClass('move-part-into');
                that.tagDiv.highlightEffect();
            }).error(function () {
                that.tagDiv.removeClass('move-part-into');
            });

        }

    });

    return TagNavItemView;
});
