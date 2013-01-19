define (function() {
    var PartMetadataView = Backbone.View.extend({

        el:$("#part_metadata_container"),

        events: {
            "click button#part_metadata_close_button": "closePartMetadata"
        },

        initialize: function() {
        },

        render: function() {

            var part_metadata_html = Mustache.render(
                $('#part_metadata_template').html(), this.model);

            this.$el.append(part_metadata_html);
            this.$(".author-popover").userPopover(this.model.getAuthorLogin(),this.model.getNumber(),"top");

            return this;
        },

        closePartMetadata: function() {
            this.$el.hide();
        }
        
    });

    return PartMetadataView;
});