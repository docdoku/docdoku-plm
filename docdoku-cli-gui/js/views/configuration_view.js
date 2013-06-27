define(["text!templates/configuration.html", "storage", "commander"], function(template, Storage, Commander) {
    var ConfigurationView = Backbone.View.extend({

        template: Handlebars.compile(template),

        events: {
            "submit #form-configuration" : "onSubmitForm",
            "hidden #configuration-modal": "onHidden",
            "click .icon-folder-open" : "openFileChooser"
        },

        render:function() {
            this.$el.html(this.template({configuration:Storage}));
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

        openFileChooser:function() {
            var self = this;
            Commander.chooseDirectory(function(folder) {
               // Méthode de callback
               self.$inputWorkingDir.val(folder);
            });
        }
    });

    return ConfigurationView;
});