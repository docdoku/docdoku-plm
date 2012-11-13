define([
    "views/part_metadata_view"
], function (
    PartMetadataView
) {

    var PartItemView = Backbone.View.extend({

        tagName:'li',

        template: _.template("<input type='checkbox' value=''><a href='#'><label class='checkbox'><%= number %> (<%= count %>)</label></a>"),

        events: {
            "click a": "showPartMetadata"
        },

        initialize: function() {
            _.bindAll(this, ["onChangeCheckbox"]);
        },

        onChangeCheckbox: function(e) {
            //find all inputs, including children inputs
            this.$("ul input").prop("checked", e.currentTarget.checked).trigger("change");
            this.model.filtered = e.currentTarget.checked;
        },

        render: function() {

            this.$el.html(this.template({number: this.model.attributes.number, count: this.model.attributes.instances.length}));

            this.input = this.$("input");

            //we can't use the events hash because we only need to bind
            //the input of this view (right now sub parts are not rendered yet)
            this.input.on("change", this.onChangeCheckbox);

            if(this.model.isNode()){
                this.$('label').addClass("isNode");
            }

            return this;
        },

        showPartMetadata:function(e) {
            e.stopPropagation();

            $("#part_metadata_container").empty();
            new PartMetadataView({model: this.model}).render();

            $("#bottom_controls_container").hide();
            $("#part_metadata_container").show();
        }

    });

    return PartItemView;

});