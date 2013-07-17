define(["text!templates/choose_path.html","i18n!localization/nls/global", "views/directory_chooser_view", "storage"],function(template, i18n, DirectoryChooserView, Storage) {

    var ChoosePathView = Backbone.View.extend({

        template: Handlebars.compile(template),

        events: {
            "submit #form-choose-path" : "onSubmitForm",
            "hidden #choose-path-modal": "onHidden"
        },

        render:function() {
            var self = this;

            this.$el.html(this.template({paths:Storage.getLocalPaths(),i18n:i18n}));
            this.bindDomElements();

            this.dcv = new DirectoryChooserView({el:this.$(".directory-chooser")}).render();

            this.dcv.on("directory:chosen",function(path){
                self.$newpath.val(path);
                self.$path.val("");
            });

            this.$path.change(function(e){
                if(e.target.value){
                    self.$newpath.val("");
                }
            });

            return this;
        },

        bindDomElements:function(){
            this.$modal = this.$('#choose-path-modal');
            this.$path = this.$('select#path');
            this.$newpath = this.$('#new-path');
        },

        onSubmitForm:function(e) {
            var path = this.$path.val() || this.$newpath.val();
            if(path){
                this.trigger("path:chosen",path);
                if(Storage.addLocalPath(path)){
                    APP_GLOBAL.SIGNALS.trigger("path:created");
                }
                this.closeModal();
            }
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

    return ChoosePathView;
});