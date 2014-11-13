define(["text!templates/remote_versioned_file.html", "views/loader",  "dplm"], function(template, Loader, Dplm) {

    var RemoteVersionedFileView = Backbone.View.extend({

        className: "versionedFile",

        template: Handlebars.compile(template),

        events: {
            "click .action-checkout"    : "checkout",
            "click .action-get"    : "download"
        },

        render:function() {
            var status = this.model.getStatus();
            status.checkoutDateParsed = moment(status.checkoutDate).format("YYYY-MM-DD HH:MM:ss");
            status.isCheckedOutByMe = this.isCheckoutByConnectedUser(status);
            status.iteration = _.last(status.iterations);
            status.canBeCheckedOut=(!status.isCheckedOut) && (!status.isReleased);
            this.$el.html(this.template({model: this.model, status: status}));

            return this;
        },

        loader:function() {
            this.$el.html(new Loader());
        },

        isCheckoutByConnectedUser:function(status) {
            return status.checkoutUser == APP_GLOBAL.GLOBAL_CONF.user;
        },

        checkout:function() {
            var self = this;
            require(["views/choose_path"],function(ChoosePathView){
                Dplm.getBaselines(self.model,{
                    success : function(baselines){
                        var choosePathView = new ChoosePathView();
                        $("body").append(choosePathView.setBaselines(baselines).render().el);
                        choosePathView.openModal();
                        choosePathView.on("path:chosen",function(choosePathViewOptions){
                            self.loader();
                            Dplm.checkout(self.model, choosePathViewOptions,{
                                success: function() {
                                    Dplm.getStatusForPart(self.model, {
                                        success: function(status) {
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
                        });
                    },
                    error:function(error){
                    }
                });
            });
        },
        download:function(){
            var self = this;
            require(["views/choose_path"],function(ChoosePathView){
                Dplm.getBaselines(self.model,{
                    success : function(baselines){
                        var choosePathView = new ChoosePathView();
                        $("body").append(choosePathView.setBaselines(baselines).render().el);
                        choosePathView.openModal();
                        choosePathView.on("path:chosen",function(choosePathViewOptions){
                            self.loader();
                            Dplm.download(self.model, choosePathViewOptions, {
                                success:  function() {
                                    self.render();
                                },
                                error:function(error){
                                }
                            });
                        });
                    },
                    error:function(error){
                    }
                });
            });
        }
    });

    return RemoteVersionedFileView;
});