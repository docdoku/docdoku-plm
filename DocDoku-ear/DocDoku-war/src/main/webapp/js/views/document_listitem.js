function get_random_color() {
	var letters = '0123456789ABCDEF'.split('');
	var color = '#';
	for (var i = 0; i < 6; i++ ) {
		color += letters[Math.round(Math.random() * 15)];
	}
	return color;
}
var DocumentListItemView = BaseView.extend({
	tagName: "tr",
	template_el: "#document-list-item-tpl",
	events: {
		"click input.select": "onSelectToggle",
		"click .reference": "showDetails",
	},
	initialize: function () {
		this.baseViewBindings();
		_.bindAll(this,
			"template", "render", "showDetails",
			"select", "onSelectToggle", "isSelected",
			"checkout", "undocheckout", "checkin",
			"delete");
			this.model.bind("change", this.render);
		this.wasSelected = false;
	},
	formatModelJSON: function (data) {
		// Format dates
		if (data.lastIteration) {
			if (data.lastIteration.documentAttributes) {
				_.each(data.lastIteration.documentAttributes, function (attribute) {
					if (attribute.type == "DATE") {
						try {
							attribute.type = new Date(attribute.type).format("dd/mm/yyyy");
						} catch (error) {}
					}
				});
			}
		}
		if (data.lastIteration && data.lastIteration.creationDate) {
			data.lastIteration.creationDate = new Date(data.lastIteration.creationDate).format("dd/mm/yyyy");
		}
		if (data.checkOutDate) {
			data.checkOutDate = new Date(data.checkOutDate).format("dd/mm/yyyy");
		}
		return data;
	},
	modelToJSON: function () {
		var data = this.model.toJSON();
		return this.formatModelJSON(data);
	},
	onModelSync: function () {
		this.render();
	},
	renderAfter: function () {
		$(this.el).find("input.select").first().attr("checked", this.wasSelected);
	},
	select: function (value) {
		this.wasSelected = value;
		$(this.el).find("input.select").first().attr("checked", value);
	},
	onSelectToggle: function () {
		this.wasSelected = $(this.el).find("input.select").first().is(":checked");
	},
	isSelected: function () {
		return $(this.el).find("input.select").first().is(":checked");
	},
	showDetails: function () {
		// TODO: Remove after the demo
		var elReference = $(this.el).find(".reference").first();
		$("#popover").html("");
		var doc = new Document({id: this.model.id});
		var that = this;
		doc.fetch({
			success: function () {
				$("#popover").html(
					Mustache.render(
						$("#document-tpl").html(),
						that.formatModelJSON(doc.toJSON())
					)
				);
				$("#popover :first-child")
					.tab()
					.css("top", elReference.offset().top - 58)
					.css("left", elReference.offset().left + 100);    
				$("#popover .arrow")
					.css("top", 75)    
					.css("left", 0);    
				$("#popover .tag_color_1").each(function () { $(this).css("background-color",get_random_color()) } ); 
				$("#popover :first-child")
					.fadeIn(100);
				$("#popover .close_popover").click(function () {
					$("#popover :first-child").fadeOut(100);
				});
			}
		});
	},
	checkout: function () {
		if (this.isSelected()) {
			this.model.checkout();
		}
	},
	undocheckout: function () {
		if (this.isSelected()) {
			this.model.undocheckout();
		}
	},
	checkin: function () {
		if (this.isSelected()) {
			this.model.checkin();
		}
	},
	delete: function () {
		if (this.isSelected()) {
			this.model.destroy();
		}
	}
});
