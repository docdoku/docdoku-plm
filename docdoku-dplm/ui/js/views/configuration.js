define(["text!templates/configuration.html","i18n!localization/nls/global","storage"],function(template, i18n, Storage) {

    var ConfigurationView = Backbone.View.extend({

        template: Handlebars.compile(template),

        events: {
            "submit #form-configuration" : "onSubmitForm",
            "hidden #configuration-modal": "onHidden"
        },

        render:function() {
            this.$el.html(this.template({configuration:Storage.getGlobalConf(), i18n:i18n}));
            this.bindDomElements();
            return this;
        },

        bindDomElements:function(){
            this.$modal = this.$('#configuration-modal');
            this.$inputHost = this.$('#inputHost');
            this.$inputPort = this.$('#inputPort');
            this.$inputUser = this.$('#inputUser');
            this.$inputPwd = this.$('#inputPwd');
            this.$inputJavaHome = this.$('#inputJavaHome');
        },

        onSubmitForm:function(e) {
            var inputHost = this.$inputHost.val();
            var inputPort = this.$inputPort.val();
            var inputUser = this.$inputUser.val();
            var inputPwd = this.$inputPwd.val();
            var inputJavaHome = this.$inputJavaHome.val();

            Storage.setGlobalConf({host:inputHost,port:inputPort,user:inputUser,password:inputPwd, javaHome:inputJavaHome});
            this.closeModal();

            APP_GLOBAL.SIGNALS.trigger("configuration:changed");

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
        }

    });

    return ConfigurationView;
});