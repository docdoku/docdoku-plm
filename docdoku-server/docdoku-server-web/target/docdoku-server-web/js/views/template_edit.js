define([
	"i18n",
	"common/date",
	"views/base",
	"views/template_new_attributes",
	"text!templates/template_edit.html"
], function (
	i18n,
	date,
	BaseView,
	TemplateNewAttributesView,
	template
) {
	var DocumentEditView = BaseView.extend({
		className: "template-edit",
		template: Mustache.compile(template),
		initialize: function () {
			BaseView.prototype.initialize.apply(this, arguments);
			// destroy previous template edit view if any
			if (DocumentEditView._instance) {
				DocumentEditView._oldInstance = DocumentEditView._instance;
			}
			// keep track of the created template edit view
			DocumentEditView._instance = this;

			this.events["click header .close"] = "closeAction";
			this.events["click footer .cancel"] = "closeAction";
			this.events["click footer .btn-primary"] = "primaryAction";
		},
		modelToJSON: function () {
			var data = this.model.toJSON();
			// Format dates
			if (data.creationDate) {
				data.creationDate = date.formatTimestamp(
					i18n._DATE_FORMAT,
					data.creationDate);
			}
			return data;
		},
		renderAt: function (offset) {
			this.offset = offset;
			if (DocumentEditView._oldInstance) {
				DocumentEditView._oldInstance.hide();
				_.delay(this.render, 100);
			} else {
				this.render();
			}
		},
		rendered: function () {
			this.$el.css("left", this.offset.x)
			this.attributesView = this.addSubView(
				new TemplateNewAttributesView({
					el: "#attributes-" + this.cid,
				})
			)
			this.attributesView.render();
			this.attributesView.collection.reset(this.model.get("attributeTemplates"));
		},
		hide: function () {
			var that = this;
			this.$el.fadeOut(250, function () {
				that.destroy();
			});
		},
		closeAction: function () {
			this.hide();
			return false;
		},
		primaryAction: function () {
			this.model.save({
				documentType: $("#form-" + this.cid + " .type").val(),
				mask: $("#form-" + this.cid + " .mask").val(),
				idGenerated: $("#form-" + this.cid + " .id-generated")
					.attr("checked") ? true : false,
				attributeTemplates: this.attributesView.collection.toJSON()
			}, {
				success: this.success,
				error: this.error
			});
			return false;
		},
	});
	return DocumentEditView;
});
