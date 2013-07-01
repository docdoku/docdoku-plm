define(["text!templates/local_versioned_file.html", "views/loader_view",  "commander", "storage"], function(template, Loader, Commander, Storage) {

    var LocalVersionedFileView = Backbone.View.extend({

        className: "versionedFile",

        template: Handlebars.compile(template),

        events: {
            "click .icon-signout"   : "checkin",
            "click .icon-signin"    : "checkout",
            "click .icon-refresh"   : "render",
            "click .icon-download"  : "get",
            "click .icon-undo"      : "undoCheckout"
        },

        render:function() {
            var status = this.model.getStatus();
            status.checkoutDateParsed = moment(status.checkoutDate).format("YYYY-MM-DD HH:MM:ss");
            status.isCheckedOutByMe = this.isCheckoutByConnectedUser(status);
            status.iteration = _.last(status.iterations);

            this.$el.html(this.template({model: this.model, status: status}));

            if (status.isCheckedOutByMe && this.model.getMTime() > status.checkoutDate) {
                this.$el.addClass("modified");
            }

            return this;
        },

        loader:function() {
            this.$el.html(new Loader());
        },

        isCheckoutByConnectedUser:function(status) {
            return status.checkoutUser == Storage.getUser();
        },

        checkin:function() {
            this.loader();
            var self = this;
            Commander.checkin(this.model.getPartNumber(), this.model.getVersion(), function() {
               Commander.getStatusForFile(self.model.getFullPath(), function(pStatus) {
                   var status = JSON.parse(pStatus);
                   self.model.setStatus(status);
                   self.render();
               });
            });
        },

        checkout:function() {
            this.loader();
            var self = this;
            console.log(this.model);
            Commander.checkout(this.model.getPartNumber(), this.model.getVersion(), function() {
                Commander.getStatusForPartNumber(self.model.getPartNumber(), self.model.getVersion(), function(pStatus) {
                    var status = JSON.parse(pStatus);
                    self.model.setStatus(status);
                    self.render();
                });
            });
        },

        undoCheckout:function() {
            this.loader();
            var self = this;
            Commander.undoCheckout(this.model.getPartNumber(), this.model.getVersion(), function() {
                Commander.getStatusForFile(self.model.getFullPath(), function(pStatus) {
                    var status = JSON.parse(pStatus);
                    self.model.setStatus(status);
                    self.render();
                });
            });
        },

        get:function() {
            this.loader();
            var self = this;
            Commander.get(this.model.getFullPath(), function() {
                Commander.getStatusForFile(self.model.getFullPath(), function(pStatus) {
                    var status = JSON.parse(pStatus);
                    self.model.setStatus(status);
                    self.render();
                });
            });
        }
    });

    return LocalVersionedFileView;
});