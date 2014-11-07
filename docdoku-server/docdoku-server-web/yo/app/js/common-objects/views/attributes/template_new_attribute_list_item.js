/*global define*/
define([
    "common-objects/views/components/list_item",
    "text!common-objects/templates/attributes/template_new_attribute_list_item.html"
], function (ListItemView, template) {
    var TemplateNewAttributeListItemView = ListItemView.extend({

        template: template,

        tagName: "div",

        initialize: function () {
            ListItemView.prototype.initialize.apply(this, arguments);
            this.events["change .type"] = "typeChanged";
            this.events["change .name"] = "updateName";
            this.events["click .fa-times"] = "removeAction";
            this.events["change .attribute-mandatory input"] = "mandatoryChanged";
        },
        rendered: function () {
            var type = this.model.get("attributeType");
            this.$el.find("select.type:first").val(type);
            this.$el.addClass("well");
        },
        removeAction: function () {
            this.model.destroy();
        },
        typeChanged: function (evt) {
            this.model.set({
                attributeType: $(evt.target).val()
            });
        },
        updateName: function () {
            this.model.set({
                name: this.$el.find("input.name:first").val()
            });
        },
        mandatoryChanged: function () {
            this.model.set({
                mandatory: this.$el.find(".attribute-mandatory input")[0].checked
            });
        }
    });
    return TemplateNewAttributeListItemView;
});
