define([
	"common-objects/views/base",
	"common-objects/views/attributes/attribute_list",
	"text!common-objects/templates/attributes/attributes.html",
    "i18n!localization/nls/document-management-strings"
], function (
	BaseView,
	AttributeListView,
	template,
    i18n
) {
	var AttributesView = BaseView.extend({

		template: Mustache.compile(template),

        editMode: true,

        attributesLocked: false,

		collection: function () {
			return new Backbone.Collection();
		},

        setEditMode: function(editMode) {
            this.editMode = editMode;
        },

        setAttributesLocked: function(attributesLocked) {
            this.attributesLocked = attributesLocked;
            this.render();
        },

		initialize: function () {
            _.bindAll(this);
			BaseView.prototype.initialize.apply(this, arguments);
			this.events["click .add"] = this.addAttribute;
		},

        render: function() {
            var data = {
                view: this.viewToJSON(),
                lockMode: this.editMode && !this.attributesLocked,
                i18n: i18n
            };
            this.$el.html(this.template(data));
            this.rendered();
            return this;
        },

		rendered: function () {
			this.attributesView = this.addSubView(
				new AttributeListView({
					el: this.$("#items-" + this.cid),
					collection: this.collection
				})
			);
            this.attributesView.setEditMode(this.editMode);
            this.attributesView.setAttributesLocked(this.attributesLocked);
		},

		addAttribute: function () {
			this.collection.add({
                mandatory: false,
				name: "",
				type: "TEXT",
				value: ""
			});
		},

        addAndFillAttribute: function(attribute){
            this.collection.add({
                mandatory: attribute.isMandatory(),
                name: attribute.getName(),
                type: attribute.getType(),
                value: attribute.getValue()
            });
        }

	});
	return AttributesView;
});
