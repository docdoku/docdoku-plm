define(["text!templates/add_path.html","i18n!localization/nls/global","views/directory_chooser_view", "storage"],function(template, i18n, DirectoryChooserView, Storage) {

    var AddPathView = Backbone.View.extend({

        template: Handlebars.compile(template),

        events: {
            "submit #form-add-path" : "onSubmitForm",
            "hidden #add-path-modal": "onHidden"
        },

        render:function() {
            var self = this;
            this.$el.html(this.template({i18n:i18n}));
            this.bindDomElements();
            this.dcv = new DirectoryChooserView({el:this.$(".directory-chooser")}).render();
            this.dcv.on("directory:chosen",function(path){self.$path.val(path)});
            return this;
        },

        bindDomElements:function(){
            this.$modal = this.$('#add-path-modal');
            this.$path = this.$('#path');
        },

        onSubmitForm:function(e) {
            var self = this ;
            var path = this.$path.val();
            console.log(path)
            fs.stat(path,function(err,stats){
                if(!err){
                    self.onSuccess(path);
                }
            });

            //Storage.addPath(path);
            e.stopPropagation();
            e.preventDefault();
            return false;
        },

        onSuccess:function(path){
            Storage.addLocalPath(path);
            APP_GLOBAL.SIGNALS.trigger("path:created");
            this.closeModal();
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

    return AddPathView;
});