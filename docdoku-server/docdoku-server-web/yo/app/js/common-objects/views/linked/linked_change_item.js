/*global define*/
define([
    'backbone',
    "mustache",
    "text!common-objects/templates/linked/linked_change_item.html"
], function (Backbone, Mustache, template) {
    var LinkedChangeItemView = Backbone.View.extend({

        tagName: "li",
        className: "linked-item well",

        events: {
            "click .delete-linked-item": "deleteButtonClicked"
        },

        initialize: function () {
        },

        render: function () {
            this.$el.html(Mustache.render(template,
                {
                    i18n: APP_CONFIG.i18n,
                    linkedItem: this.model,
                    editMode: this.options.editMode
                }
            ));

            if (this.model.getPriority) {
                this.$el.addClass("priorityColor-" + this.model.getPriority());
            }

            return this;
        },

        deleteButtonClicked: function () {
            this.model.collection.remove(this.model);
            this.remove();
            return false;
        }

    });
    return LinkedChangeItemView;
});

