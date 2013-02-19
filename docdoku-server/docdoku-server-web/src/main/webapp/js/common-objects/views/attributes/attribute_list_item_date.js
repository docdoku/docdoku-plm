define([
	"i18n!localization/nls/document-management-strings",
	"common-objects/views/attributes/attribute_list_item",
	"text!common-objects/templates/attributes/attribute_list_item.html",
	"text!common-objects/templates/attributes/attribute_list_item_date.html",
    "common-objects/utils/date"
], function (
	i18n,
	AttributeListItemView,
	attribute_list_item,
	template,
    date
) {
	var AttributeListItemDateView = AttributeListItemView.extend({

		template: Mustache.compile(template),

		partials: {
			attribute_list_item: attribute_list_item
		},

		initialize: function () {
            AttributeListItemView.prototype.initialize.apply(this, arguments);
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

	return AttributeListItemDateView;

});
