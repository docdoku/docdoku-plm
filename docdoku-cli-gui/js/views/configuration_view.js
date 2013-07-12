define(["text!templates/configuration.html",
        "i18n!localization/nls/global",
        "storage",
        "commander",
        "views/directory_chooser_view"
],
    function(template, i18n, Storage, Commander, DirectoryChooserView) {
    var ConfigurationView = Backbone.View.extend({

        template: Handlebars.compile(template),

        events: {
            "submit #form-configuration" : "onSubmitForm",
            "hidden #configuration-modal": "onHidden",
            "click #open-directory-chooser" : "openDirectoryChooser"
        },

        render:function() {
            this.$el.html(this.template({configuration:Storage, i18n:i18n}));
            this.bindDomElements();

            return this;
        },

        bindDomElements:function(){
            this.$modal = this.$('#configuration-modal');
            this.$inputHost = this.$('#inputHost');
            this.$inputPort = this.$('#inputPort');
            this.$inputUser = this.$('#inputUser');
            this.$inputPwd = this.$('#inputPwd');
            this.$inputWorkspace = this.$('#inputWorkspace');
            this.$inputWorkingDir = this.$('#inputWorkingDir');
            this.$directoryChooser = this.$('#directory_chooser');
        },

        onSubmitForm:function(e) {
            var inputHost = this.$inputHost.val();
            var inputPort = this.$inputPort.val();
            var inputUser = this.$inputUser.val();
            var inputPwd = this.$inputPwd.val();
            var inputWorkspace = this.$inputWorkspace.val();
            var inputWorkingDir = this.$inputWorkingDir.val();

            Storage.setConfig(inputHost, inputPort, inputUser, inputPwd, inputWorkspace, inputWorkingDir);
            this.closeModal();

            // Evènement pour les autres vues : si la conf change
            this.trigger("config:changed");

            e.stopPropagation();
            e.preventDefault();

            return false;
        },

        openModal: function() {
            this.$modal.modal('show');
        },

        closeModal: function() {
            this.$modal.modal('hide');
        },

        onHidden: function() {
            this.remove();
        },

        persist:function() {

        },

        openDirectoryChooser:function() {

            var self = this ;

            var dcv = new DirectoryChooserView({el:this.$directoryChooser}).render();

            this.$directoryChooser.removeClass("hide");

            dcv.on("directory:chosen",function(folder){
                self.$inputWorkingDir.val(folder);
            });

        }
    });

    return ConfigurationView;
});