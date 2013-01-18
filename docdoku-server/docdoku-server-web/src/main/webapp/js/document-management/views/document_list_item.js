define([
	"i18n",
	"common/date",
	"views/checkbox_list_item",
    "views/iteration/document_iteration",
	"text!templates/document_list_item.html"
], function (
	i18n,
	date,
	CheckboxListItemView,
    IterationView,
	template
) {
	var DocumentListItemView = CheckboxListItemView.extend({

		template: Mustache.compile(template),

		tagName: "tr",

		initialize: function () {
			CheckboxListItemView.prototype.initialize.apply(this, arguments);
			this.events["click .reference"] = this.actionEdit;
		},

		modelToJSON: function () {
			var data = this.model.toJSON();
			if (this.model.hasIterations()) {
				data.lastIteration = this.model.getLastIteration().toJSON();
                data.lastIteration.creationDate = date.formatTimestamp(
                    i18n._DATE_FORMAT,
                    data.lastIteration.creationDate
                );
			}

            if (this.model.isCheckout()) {
                data.checkOutDate = date.formatTimestamp(
                    i18n._DATE_FORMAT,
                    data.checkOutDate
                );
            }

			return data;
		},

		actionEdit: function (evt) {
			var that = this;
			this.model.fetch().success(function () {
                new IterationView({
                    model: that.model
                }).show();
            });
        }

    });

    return DocumentListItemView;
});
