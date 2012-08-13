window.PartMetadataView = Backbone.View.extend({

    el:$("#part_metadata_container"),

    events: {
        "click button#part_metadata_close_button"   : "closePartMetadata"
    },

    render: function(){

        var part_metadata_html = Mustache.render(
            $('#part_metadata_template').html(), this.model);

        this.$el.append(part_metadata_html);
    },

    closePartMetadata: function(){
        $("#part_metadata_container").slideUp();
    }

});