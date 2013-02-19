define([
	"common-objects/views/components/list_item"
], function (
	ListItemView
) {
	var AttributeListItemView = ListItemView.extend({

		tagName: "div",

        editMode: true,

        setEditMode: function(editMode) {
            this.editMode = editMode;
        },

		initialize: function() {
			ListItemView.prototype.initialize.apply(this, arguments);
			this.events[ "change .type"] = "typeChanged";
			this.events[ "change .name"] = "updateName";
			this.events[ "change .value"] = "updateValue";
			this.events[ "click .remove"] = "removeAction";
		},

		rendered: function() {
			var type = this.model.get("type");
			this.$el.find("select.type:first").val(type);
		},

		removeAction: function() {
			this.model.destroy();
		},

		typeChanged: function(evt) {
			var type = $(evt.target).val();
			this.model.set({
				type: type,
				value: "" // TODO: Validate and convert if possible between types
			});
			this.model.collection.trigger("reset");
		},

		updateName: function() {
			this.model.set({
				name: this.$el.find("input.name:first").val()
			});
		},

		updateValue: function() {
			var el = this.$el.find("input.value:first");
			this.model.set({
				value: this.getValue(el)
			});
		},

		getValue: function(el) {
			return el.val();
		},

        render: function() {
            this.deleteSubViews();
            var partials = this.partials ? this.partials : null;
            var data = this.renderData();
            data.editMode = this.editMode;
            this.$el.html(this.template(data, partials));
            this.rendered();
            return this;
        }

	});

	return AttributeListItemView;
});
