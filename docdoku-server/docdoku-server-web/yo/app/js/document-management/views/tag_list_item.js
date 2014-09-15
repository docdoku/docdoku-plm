/*global define,App*/
define([
    "common-objects/views/components/list_item",
    "views/tag_document_list",
    "text!templates/tag_list_item.html"
], function (ListItemView, TagDocumentListView, template) {
    var TagListItemView = ListItemView.extend({

        template: template,
        tagName: "li",
        className: "tag",
        initialize: function () {
            ListItemView.prototype.initialize.apply(this, arguments);
            this.events = _.extend(this.events, {
                "click .delete": "actionDelete",
                "mouseleave .header": "hideActions"
            });

        },
        hideActions: function () {
            // Prevents the actions menu to stay opened all the time
            this.$(".header .btn-group").first().removeClass("open");
        },
        setActive: function () {
            if (App.$documentManagementMenu) {
                App.$documentManagementMenu.find(".active").removeClass("active");
            }
            this.$(".nav-list-entry").first().addClass("active");
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
            if (confirm(APP_CONFIG.i18n.DELETE_TAG_QUESTION)) {
                this.model.destroy();
            }
            return false;
        }
    });
    return TagListItemView;
});
