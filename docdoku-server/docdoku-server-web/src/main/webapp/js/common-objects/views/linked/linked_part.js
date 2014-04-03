define([
    "text!common-objects/templates/linked/linked_part.html",
    "i18n!localization/nls/product-management-strings"
], function (
    template,
    i18n
    ) {
    var LinkedPartView = Backbone.View.extend({

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
                    linkedPart: this.model,
                    editMode: this.options.editMode
                }
            ));

            return this;
        },

        deleteButtonClicked: function() {
            this.model.collection.remove(this.model);
            this.remove();
            return false;
        }

    });
    return LinkedPartView;
});

