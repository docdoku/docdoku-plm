define([
    "text!common-objects/templates/linked/linked_document.html",
    "i18n!localization/nls/document-management-strings"
], function (
    template,
    i18n
    ) {
    var LinkedDocumentView = Backbone.View.extend({

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
                    linkedDocument: this.model,
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
    return LinkedDocumentView;
});

