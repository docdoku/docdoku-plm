/*global _,define,App*/
define([
    'common-objects/views/attributes/attribute_list_item',
    'text!common-objects/templates/attributes/attribute_list_item.html',
    'text!common-objects/templates/attributes/attribute_list_item_date.html',
    'common-objects/utils/date'
], function (AttributeListItemView, attributeListItem, template, date) {
	'use strict';
    var AttributeListItemDateView = AttributeListItemView.extend({

        template: template,

        partials: {
            attributeListItem: attributeListItem
        },

        initialize: function () {
            AttributeListItemView.prototype.initialize.apply(this, arguments);
            this.templateExtraData = {
                timeZone : App.config.timeZone,
                language : App.config.locale
            };
        },

        /**
         * format date from attribute model (timestamp string) to html5 input date ('yyyy-mm-dd')
         */
        modelToJSON: function () {
            var format = this.editMode ? App.config.i18n._DATE_PICKER_DATE_FORMAT
                : App.config.i18n._DATE_SHORT_FORMAT;
            var data = this.model.toJSON();
            if (!_.isEmpty(data.value)) {
                data.value = date.formatLocalTime(
                    format,
                    new Date(data.value)
                );
            }
            return data;
        },

        /**
         * format date from html5 input to timestamp string
         */
        getValue: function (el) {
            return date.formatLocalTime('YYYY-MM-DDTHH:mm:ss',el.val());
        },

        /**
         * called on input change
         */
        updateValue: function () {
            var el = this.$('input.value');
            this.model.set({
                value: this.getValue(el)
            }, {
                silent: true
            });
        }

    });

    return AttributeListItemDateView;
});
