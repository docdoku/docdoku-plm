DocumentNewView = ModalView.extend({
	template: "document-new-tpl",
	initialize: function () {
		ModalView.prototype.initialize.apply(this, arguments);
		this.model = new Document();
		this.events = _.extend(this.events, {
			"submit .main-form": "primaryAction",
		});
	},
	rendered: function () {
		this.attributesView = this.addSubView(new DocumentNewAttributeListView({
			el: "#attributes-" + this.cid,
		}));
		this.attributesView.render();
		this.templatesView = this.addSubView(new DocumentNewTemplateListView({
			el: "#templates-" + this.cid,
			attributesView: this.attributesView
		}));
		this.templatesView.collection.fetch();
	},
	primaryAction: function () {
		/*
		var reference = $(".main-form reference").val();
		if (reference) {
			this.collection.create({
				reference: reference,
				title: $("#form-" + this.cid + " .title").val(),
				description: $("#form-" + this.cid + " .description").val(),
			}, {
				success: this.success,
				error: this.error
			});
		}
		*/
		return false;
	},
	success: function (model, response) {
		/*
		var iterationData = model.get("lastIteration");
		iterationData.id = iterationData.iteration;
		var iteration = new DocumentIteration(iterationData);
		iteration.save();
		*/
		//this.collection.fetch();
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
