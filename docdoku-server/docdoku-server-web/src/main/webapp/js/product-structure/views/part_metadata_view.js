define (function() {
    var PartMetadataView = Backbone.View.extend({

        el:$("#part_metadata_container"),

        events: {
            "click button#part_metadata_close_button": "closePartMetadata"
        },

        initialize: function() {
            this.webRtcModal = $("#webRtcModal");
            this.webRtcModalBody = this.webRtcModal.find(".modal-body");
            this.webRtcModalTitle = this.webRtcModal.find("h3");
            _.bindAll(this, ["callAuthor"]);
        },

        render: function() {

            var part_metadata_html = Mustache.render(
                $('#part_metadata_template').html(), this.model);

            this.$el.append(part_metadata_html);

            var authorBlock = this.$("#part_metadata_author a");
            authorBlock.click(this.callAuthor);

            return this;
        },

        closePartMetadata: function() {
            this.$el.hide();
        },

        callAuthor: function() {
            var self = this;
            this.webRtcModal.one('shown', function() {
                self.webRtcModalBody.html("<iframe src=\""+ self.model.getWebRtcUrlRoom() +"\" />");
                self.webRtcModalTitle.text("Call to " + self.model.getAuthor());
            });
            this.webRtcModal.one('hidden', function() {
                self.webRtcModalBody.empty();
            });
            this.webRtcModal.modal('show');
        }

    });

    return PartMetadataView;
});