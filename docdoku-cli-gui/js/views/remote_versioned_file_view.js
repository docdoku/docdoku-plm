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
            return status.checkoutUser == APP_GLOBAL.GLOBAL_CONF.user;
        },

        checkout:function() {
            var self = this;
            require(["views/choose_path_view"],function(ChoosePathView){
                var choosePathView = new ChoosePathView();
                $("body").append(choosePathView.render().el);
                choosePathView.openModal();
                choosePathView.on("path:chosen",function(path){
                    APP_GLOBAL.CURRENT_PATH = path;
                    self.loader();
                    Commander.checkout(self.model, function() {
                        Commander.getStatusForPart(self.model, function(pStatus) {
                            var status = JSON.parse(pStatus);
                            self.model.setStatus(status);
                            self.remove();
                        });
                    });
                });
            });


        }
    });

    return RemoteVersionedFileView;
});