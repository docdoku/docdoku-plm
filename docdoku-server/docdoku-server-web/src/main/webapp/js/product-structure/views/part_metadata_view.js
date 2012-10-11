define (function() {
    var PartMetadataView = Backbone.View.extend({

        el:$("#part_metadata_container"),

        events: {
            "click button#part_metadata_close_button"   : "closePartMetadata"
        },

        initialize: function() {
            this.webRtcModal = $("#webRtcModal");
            this.webRtcModalBody = this.webRtcModal.find(".modal-body");
            _.bindAll(this, ["callAuthor"]);
        },

        render: function(){

            var part_metadata_html = Mustache.render(
                $('#part_metadata_template').html(), this.model);

            this.$el.append(part_metadata_html);

            var authorBlock = this.$("#part_metadata_author a");
            authorBlock.click(this.callAuthor);

            return this;
        },

        closePartMetadata: function(){
            $("#part_metadata_container").hide();
            $("#bottom_controls_container").show();
        },

        callAuthor: function() {
            var self = this;
            this.webRtcModal.one('shown', function() {
                self.webRtcModalBody.html("<iframe src=\""+ self.model.getWebRtcUrlRoom() +"\" />");
            });
            this.webRtcModal.one('hidden', function() {
                self.webRtcModalBody.empty();
            });
            this.webRtcModal.modal('show');
        }

    });

    return PartMetadataView;
});