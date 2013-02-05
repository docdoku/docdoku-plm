define(["views/bom_header_view", "views/bom_content_view"], function (BomHeaderView, BomContentView) {

    var BomView = Backbone.View.extend({

        render: function() {
            this.bomContentView = new BomContentView().render();
            this.bomHeaderView = new BomHeaderView().render();
            this.listenTo(this.bomContentView, "itemSelectionChanged", this.bomHeaderView.onSelectionChange);
            return this;
        },

        updateContent: function(component) {
            this.bomContentView.update(component);
        }

    });

    return BomView;

});
