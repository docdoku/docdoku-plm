define(["text!templates/choose_path.html","i18n!localization/nls/global", "views/directory_chooser", "storage"],function(template, i18n, DirectoryChooserView, Storage) {

    var ChoosePathView = Backbone.View.extend({

        template: Handlebars.compile(template),

        events: {
            "submit #form-choose-path" : "onSubmitForm",
            "hidden #choose-path-modal": "onHidden"
        },

        setBaselines:function(baselines){
            this.baselines = baselines
            return this;
        },

        render:function() {
            var self = this;

            this.$el.html(this.template({baselines:this.baselines,paths:Storage.getLocalPaths(),i18n:i18n}));
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
            this.$force = this.$('#force');
            this.$recursive = this.$('#recursive');
            this.$baseline = this.$('#baseline');
        },

        onSubmitForm:function(e) {
            var path = this.$path.val() || this.$newpath.val();

            var options = {
                path:path,
                force:this.$force.is(":checked"),
                recursive:this.$recursive.is(":checked"),
                baseline:this.$baseline.val()
            };

            if(path){
                if(Storage.addLocalPath(path)){
                    APP_GLOBAL.SIGNALS.trigger("path:created");
                }
                APP_GLOBAL.SIGNALS.trigger("path:changed", path);
                this.trigger("path:chosen",options);
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