/*global define,bootbox,App,_*/
define([
    'common-objects/views/components/list_item',
    'views/tag_document_list',
    'text!templates/tag_list_item.html',
    'common-objects/models/document/document_revision'
], function (ListItemView, TagDocumentListView, template, DocumentRevision) {
    'use strict';
    var TagListItemView = ListItemView.extend({

        template: template,
        tagName: 'li',
        className: 'tag',
        initialize: function () {
            ListItemView.prototype.initialize.apply(this, arguments);
            this.events = _.extend(this.events, {
                'click .delete': 'actionDelete',
                'mouseleave .header': 'hideActions',
                'dragenter >.nav-list-entry': 'onDragEnter',
                'dragover >.nav-list-entry': 'checkDrag',
                'dragleave >.nav-list-entry': 'onDragLeave',
                'drop >.nav-list-entry': 'onDrop'
            });

        },
        rendered: function () {
            this.tagDiv = this.$('>.nav-list-entry');
        },
        hideActions: function () {
            // Prevents the actions menu to stay opened all the time
            this.$('.header .btn-group').first().removeClass('open');
        },
        setActive: function () {
            if (App.$documentManagementMenu) {
                App.$documentManagementMenu.find('.active').removeClass('active');
            }
            this.$('.nav-list-entry').first().addClass('active');
        },
        showContent: function () {
            this.setActive();
            this.addSubView(
                new TagDocumentListView({
                    model: this.model
                })
            ).render();
        },
        actionDelete: function () {
            this.hideActions();
            var that = this;
            bootbox.confirm(App.config.i18n.DELETE_TAG_QUESTION,
                App.config.i18n.CANCEL,
                App.config.i18n.DELETE,
                function (result) {
                    if (result) {
                        that.model.destroy({
                            dataType: 'text' // server doesn't send a json hash in the response body
                        });
                    }
                });
            return false;
        },
        onDragEnter: function () {
            this.tagDiv.hasClass('move-doc-into');
        },

        checkDrag: function (e) {
            e.dataTransfer.dropEffect = 'copy';
            this.tagDiv.addClass('move-doc-into');
            return false;
        },

        onDragLeave: function (e) {
            e.dataTransfer.dropEffect = 'none';
            this.tagDiv.removeClass('move-doc-into');
        },

        onDrop: function (e) {
            if (e.dataTransfer.getData('document:text/plain')) {
                this.tagDocument(e);
            }
        },

        tagDocument: function (e) {
            var that = this;
            var documentRevision = new DocumentRevision(JSON.parse(e.dataTransfer.getData('document:text/plain')));

            documentRevision.addTags([this.model]).success(function () {
                that.tagDiv.removeClass('move-doc-into');
                that.tagDiv.highlightEffect();
            }).error(function () {
                that.tagDiv.removeClass('move-doc-into');
            });

        }
    });
    return TagListItemView;
});
