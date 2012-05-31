define([
	"i18n",
	"common/date",
	"views/checkbox_list_item",
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
		modelToJSON: function () {
			var data = this.model.toJSON();
			// Format dates
			data.creationDate = date.formatTimestamp(
				i18n._DATE_FORMAT,
				data.creationDate);
			return data;
		},
		actionEdit: function (evt) {
			var that = this;
			var target = $(evt.target); 
			var targetOffset = target.offset(); 
			var offset = {
				x: targetOffset.left + target.width(),
				y: targetOffset.top + (target.height() / 2)
			};
			this.model.fetch().success(function () {
				that.editView = that.addSubView(
					new TemplateEditView({
						model: that.model
					})
				);
				$("#content").append(that.editView.el);
				that.editView.renderAt(offset);
			});
		},
	});
	return TemplateListItemView;
});
