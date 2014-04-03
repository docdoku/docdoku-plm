define([
	"i18n!localization/nls/document-management-strings",
	"common-objects/utils/date",
    "common-objects/views/documents/checkbox_list_item",
	"views/template_edit",
	"text!templates/template_list_item.html"
], function (
	i18n,
	date,
	CheckboxListItemView,
	TemplateEditView,
	template
) {
	var TemplateListItemView = CheckboxListItemView.extend({
		template: Mustache.compile(template),
		tagName: "tr",
		initialize: function () {
			CheckboxListItemView.prototype.initialize.apply(this, arguments);
			this.events["click .reference"] = this.actionEdit;
		},

        rendered: function() {
            CheckboxListItemView.prototype.rendered.apply(this, arguments);
            this.$(".author-popover").userPopover(this.model.attributes.author.login, this.model.id, "left");
        },

		modelToJSON: function () {
			var data = this.model.toJSON();
			// Format dates
            if(!_.isUndefined(data.creationDate)){
                data.creationDate = date.formatTimestamp(
                    i18n._DATE_FORMAT,
                    data.creationDate);
            }
			return data;
		},
		actionEdit: function (evt) {
			var that = this;
			this.model.fetch().success(function () {
				that.editView = that.addSubView(
					new TemplateEditView({
						model: that.model
					})
				);
				$("body").append(that.editView.el);
			});
		}
	});
	return TemplateListItemView;
});
