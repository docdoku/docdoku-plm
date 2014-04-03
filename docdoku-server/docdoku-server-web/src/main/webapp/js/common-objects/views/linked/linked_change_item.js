define([
    "text!common-objects/templates/linked/linked_change_item.html",
    "i18n!localization/nls/change-management-strings"
], function (
    template,
    i18n
    ) {
    var LinkedChangeItemView = Backbone.View.extend({

        tagName: "li",
        className: "linked-item well",

        events: {
            "click .delete-linked-item" : "deleteButtonClicked"
        },

        initialize: function () {
        },

        render: function() {
            this.$el.html(Mustache.render(template,
                {
                    i18n: i18n,
                    linkedItem: this.model,
                    editMode: this.options.editMode
                }
            ));

            if(this.model.getPriority){
                this.$el.addClass("priorityColor-"+this.model.getPriority());
            }

            return this;
        },

        deleteButtonClicked: function() {
            this.model.collection.remove(this.model);
            this.remove();
            return false;
        }

    });
    return LinkedChangeItemView;
});

