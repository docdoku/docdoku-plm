/*global define*/
define([
    'common-objects/views/attributes/attribute_list_item',
    'text!common-objects/templates/attributes/attribute_list_item.html',
    'text!common-objects/templates/attributes/attribute_list_item_part_number.html'
], function (AttributeListItemView, attributeListItem, template) {
    'use strict';
    var AttributeListItemPartNumberView = AttributeListItemView.extend({

        template: template,
        partials: {
            attributeListItem: attributeListItem
        },
        initialize: function () {
            AttributeListItemView.prototype.initialize.apply(this, arguments);
        },
        render: function () {
            AttributeListItemView.prototype.render.apply(this, arguments);
            this.bindPartNumberTypeAhead();
        },
        updateValue: function () {

        },
        bindPartNumberTypeAhead: function () {
            var that=this;
            this.$el.find('input.value:first').typeahead({
                source: this.source.bind(this),
                updater: function (part) {
                    that.model.set({
                        value: part.split('<')[1].replace('>', '').trim()
                    });
                }
            });
        },
        source: function (query, process) {
            $.getJSON(App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/numbers?q=' + query, function (data) {
                var partNumbers = _(data).map(function (d) {
                   return d.partName + ' < ' + d.partNumber + ' >';
                });
                process(partNumbers);
            });
        }


    });
    return AttributeListItemPartNumberView;
});
