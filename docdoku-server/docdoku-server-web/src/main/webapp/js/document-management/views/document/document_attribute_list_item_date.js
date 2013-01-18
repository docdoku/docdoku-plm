define([
	"i18n!localization/nls/document-management-strings",
	"views/document/document_attribute_list_item",
	"text!templates/partials/document_attribute_list_item.html",
	"text!templates/document/document_attribute_list_item_date.html",
    "common/date"
], function (
	i18n,
	DocumentAttributeListItemView,
	document_attribute_list_item,
	template,
    date
) {
	var DocumentAttributeListItemDateView = DocumentAttributeListItemView.extend({

		template: Mustache.compile(template),

		partials: {
			document_attribute_list_item: document_attribute_list_item
		},

		initialize: function () {
            DocumentAttributeListItemView.prototype.initialize.apply(this, arguments);
		},

        /**
         * format date from attribute model (timestamp string) to html5 input date ("yyyy-mm-dd")
         */
		modelToJSON: function () {
			var data = this.model.toJSON();
            if (!_.isEmpty(data.value)) {
                data.value =  date.formatTimestamp(
                    i18n._DATE_PICKER_DATE_FORMAT,
                    parseInt(data.value, 10)
                );
            }
			return data;
		},

        /**
         * format date from html5 input to timestamp string
         */
		getValue: function (el) {
            return new Date(el.val()).getTime().toString();
		},

        /**
         * called on input change
         */
        updateValue: function() {
            var el = this.$("input.value");
            this.model.set({
                value: this.getValue(el)
            }, {
                silent: true
            });
        }

	});

	return DocumentAttributeListItemDateView;

});
