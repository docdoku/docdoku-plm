/*global define*/
define([
    'backbone',
    "mustache",
    "text!common-objects/templates/linked/linked_document.html"
], function (Backbone, Mustache, template) {
    var LinkedDocumentView = Backbone.View.extend({

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
                    i18n: App.config.i18n,
                    linkedDocument: this.model,
                    editMode: this.options.editMode
                }
            ));

            return this;
        },

        deleteButtonClicked: function () {
            this.model.collection.remove(this.model);
            this.remove();
            return false;
        }

    });
    return LinkedDocumentView;
});

