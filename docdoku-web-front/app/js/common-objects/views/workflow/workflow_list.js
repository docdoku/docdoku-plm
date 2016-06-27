/*global define*/
define([
    'common-objects/collections/workflow_models',
    'common-objects/views/base',
    'text!common-objects/templates/workflow/workflow_select.html'
], function (WorkflowList, BaseView, template) {

    'use strict';

    var DocumentWorkflowListView = BaseView.extend({

        template: template,

        initialize: function () {
            BaseView.prototype.initialize.apply(this, arguments);
            this.events['change #select-' + this.cid] = 'onChange';
        },

        onChange: function () {
            this.trigger('workflow:change', this.selected());
        },

        collection: function () {
            var collection = new WorkflowList();
            collection.fetch({reset: true});
            return collection;
        },

        collectionReset: function () {
            this.render();
        },

        collectionToJSON: function () {
            var data = BaseView.prototype.collectionToJSON.call(this);
            data.unshift({
                id: ''
            });
            return data;
        },

        selected: function () {
            var id = this.$('#select-' + this.cid).val();
            var model = this.collection.get(id);
            return model;
        },

        setValue: function (value) {
            this.$('#select-' + this.cid).val(value);
            this.trigger('workflow:change', this.selected());
        }

    });

    return DocumentWorkflowListView;
});
