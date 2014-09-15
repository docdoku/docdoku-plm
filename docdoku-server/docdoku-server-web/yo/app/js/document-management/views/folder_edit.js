/*global define*/
define([
    "mustache",
    "common-objects/views/components/modal",
    "text!templates/folder_edit.html"
], function (Mustache, ModalView, template) {
    var FolderEditView = ModalView.extend({

        template: template,

        tagName: "div",
        initialize: function () {
            ModalView.prototype.initialize.apply(this, arguments);
            this.events["submit form"] = "primaryAction";
        },
        rendered: function () {
            this.nameInput = this.$el.find("input.name").first();
            this.nameInput.val(this.model.get("name"));
        },
        primaryAction: function () {
            var name = $.trim(this.nameInput.val());

            if (name != this.model.get("name") && name != "") {
                this.model.save({
                    name: name
                }, {
                    success: this.success,
                    error: this.error
                });
            } else {
                this.hide();
            }

            return false;
        },
        success: function (model, response) {
            this.model.id = response.id;
            this.hide();
        },
        error: function (model, error) {
            if (error.responseText) {
                this.alert({
                    type: "error",
                    message: error.responseText
                });
            } else {
                console.error(error);
            }
        }
    });
    return FolderEditView;
});
