define(["text!templates/remote_versioned_file.html", "views/loader_view",  "commander", "storage"], function(template, Loader, Commander, Storage) {

    var RemoteVersionedFileView = Backbone.View.extend({

        className: "versionedFile",

        template: Handlebars.compile(template),

        events: {
            "click .icon-signin"    : "checkout"
        },

        render:function() {
            var status = this.model.getStatus();
            status.checkoutDateParsed = moment(status.checkoutDate).format("YYYY-MM-DD HH:MM:ss");
            status.isCheckedOutByMe = this.isCheckoutByConnectedUser(status);
            status.iteration = _.last(status.iterations);

            this.$el.html(this.template({model: this.model, status: status}));

            return this;
        },

        loader:function() {
            this.$el.html(new Loader());
        },

        isCheckoutByConnectedUser:function(status) {
            return status.checkoutUser == Storage.getUser();
        },

        checkout:function() {
            this.loader();
            var self = this;
            Commander.checkout(this.model.getPartNumber(), this.model.getVersion(), function() {
                Commander.getStatusForPartNumber(self.model.getPartNumber(), self.model.getVersion(), function(pStatus) {
                    var status = JSON.parse(pStatus);
                    self.model.setStatus(status);
                    self.remove();
                });
            });
        }
    });

    return RemoteVersionedFileView;
});