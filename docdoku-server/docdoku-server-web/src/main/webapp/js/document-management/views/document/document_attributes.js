define([
	"views/base",
	"views/document/document_attribute_list",
	"text!templates/document/document_attributes.html",
    "i18n!localization/nls/document-management-strings"
], function (
	BaseView,
	DocumentAttributeListView,
	template,
    i18n
) {
	var DocumentAttributesView = BaseView.extend({

		template: Mustache.compile(template),

        editMode: true,

		collection: function () {
			return new Backbone.Collection();
		},

        setEditMode: function(editMode) {
            this.editMode = editMode;
        },

		initialize: function () {
			BaseView.prototype.initialize.apply(this, arguments);
			this.events["click .add"] = this.addAttribute;
		},

        render: function() {
            var data = {
                view: this.viewToJSON(),
                editMode: this.editMode,
                i18n: i18n
            };
            this.$el.html(this.template(data));
            this.rendered();
            return this;
        },

		rendered: function () {
			this.attributesView = this.addSubView(
				new DocumentAttributeListView({
					el: "#items-" + this.cid,
					collection: this.collection
				})
			);
            this.attributesView.setEditMode(this.editMode);
		},

		addAttribute: function () {
			this.collection.add({
				name: "",
				type: "TEXT",
				value: ""
			});
		},

        addAndFillAttribute: function(attribute){
            this.collection.add({
                name: attribute.getName(),
                type: attribute.getType(),
                value: attribute.getValue()
            });
        }

	});
	return DocumentAttributesView;
});
