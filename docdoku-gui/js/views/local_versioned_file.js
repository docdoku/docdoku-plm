define(["text!templates/local_versioned_file.html", "views/loader",  "dplm"], function(template, Loader, Dplm) {

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

            if (status.lastModified && this.model.getMTime() > status.lastModified) {
                this.$el.addClass("modified");
            }

            return this;
        },

        loader:function() {
            this.$el.html(new Loader());
        },

        isCheckoutByConnectedUser:function(status) {
            return status.checkoutUser == APP_GLOBAL.GLOBAL_CONF.user;
        },

        checkin:function() {
            this.loader();
            var self = this;
            Dplm.checkin(this.model, {
                success : function() {
                    Dplm.getStatusForFile(self.model.getFullPath(), {
                        success : function(status) {
                            self.model.setStatus(status);
                            self.render();
                        },
                        error:function(error){
                        }
                    });
                },
                error:function(error){
                }
            });
        },

        checkout:function() {
            this.loader();
            var self = this;
            Dplm.checkout(this.model,{},{
                success :  function() {
                    Dplm.getStatusForPart(self.model, {
                        success : function(status) {
                            self.model.setStatus(status);
                            self.render();
                        },
                        error:function(error){
                        }
                    });
                },
                error:function(error){
                }
            });
        },

        undoCheckout:function() {
            this.loader();
            var self = this;
            Dplm.undoCheckout(this.model,{
                success : function() {
                    Dplm.getStatusForFile(self.model.getFullPath(),{
                        success :  function(status) {
                            self.model.setStatus(status);
                            self.render();
                        },
                        error:function(error){
                        }
                    });
                },
                error:function(error){
                }
            });
        },

        download:function() {
            this.loader();
            var self = this;
            Dplm.download(this.model.getFullPath(),{
                success: function() {
                    Dplm.getStatusForFile(self.model.getFullPath(),{
                        success : function(status) {
                            self.model.setStatus(status);
                            self.render();
                        },
                        error:function(error){
                        }
                    });
                },
                error:function(error){
                }
            });
        }
    });

    return LocalVersionedFileView;
});