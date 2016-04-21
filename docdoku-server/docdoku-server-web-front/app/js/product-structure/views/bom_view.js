/*global define*/
define(['backbone', 'views/bom_header_view', 'views/bom_content_view'], function (Backbone, BomHeaderView, BomContentView) {
    'use strict';
    var BomView = Backbone.View.extend({

        render: function () {
            this.bomContentView = new BomContentView().render();
            this.bomHeaderView = new BomHeaderView().render();
            this.listenTo(this.bomContentView, 'itemSelectionChanged',this.onSelectionChange);
            this.listenTo(this.bomHeaderView, 'actionCheckout', this.bomContentView.actionCheckout);
            this.listenTo(this.bomHeaderView, 'actionUndocheckout', this.bomContentView.actionUndocheckout);
            this.listenTo(this.bomHeaderView, 'actionCheckin', this.bomContentView.actionCheckin);
            this.listenTo(this.bomHeaderView, 'actionUpdateACL', this.bomContentView.actionUpdateACL);
            return this;
        },

        onSelectionChange: function(list) {
            this.trigger('checkbox:change');
            this.bomHeaderView.onSelectionChange(list);
        },

        uncheckAll: function(list) {
            if(list.length) {
                this.bomContentView.setCheckAll(false);
                this.bomHeaderView.onSelectionChange([]);
            }
            this.bomHeaderView.onPathChange(list);
        },

        updateContent: function (component) {
            this.bomContentView.update(component);
        },

        showRoot: function (component) {
            this.bomContentView.showRoot(component);
        }

    });

    return BomView;

});
