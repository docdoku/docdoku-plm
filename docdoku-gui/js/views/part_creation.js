define(
    [
        "text!templates/part_creation.html",
        "i18n!localization/nls/product-creation-strings",
        "models/part",
        "dplm"
    ],
    function (template, i18n, Part, Dplm) {

    var PartCreationView = Backbone.View.extend({

        events: {
            "submit #part_creation_form" : "onSubmitForm",
            "hidden #part_creation_modal": "onHidden"
        },

        template: Handlebars.compile(template),

        initialize: function() {
            _.bindAll(this);
        },

        render: function() {
            var self = this ;

            Dplm.getWorkspaces({
                success:function(workspaces){
                    self.$el.html(self.template({i18n : i18n, workspaces:workspaces}));
                    self.bindDomElements();
                    self.openModal();
                },
                error:function(error){
                }
            });

            return this;
        },

        bindDomElements:function(){
            this.$modal = this.$('#part_creation_modal');
            this.$inputPartNumber = this.$('#inputPartNumber');
            this.$inputPartName = this.$('#inputPartName');
            this.$inputPartDescription = this.$('#inputPartDescription');
            this.$inputWorkspace = this.$('#inputWorkspace');
        },

        onSubmitForm: function(e) {

            var part = new Part({
                number: this.$inputPartNumber.val(),
                name:  this.$inputPartName.val(),
                description: this.$inputPartDescription.val(),
                workspace:this.$inputWorkspace.val()
            });

            var self = this;

            Dplm.createPart(part, this.model.getFullPath(),{
                success:function() {
                    self.closeModal();
                    self.trigger("part:created",self.model);
                },
                error:function(error){
                }
            });

            e.preventDefault();
            e.stopPropagation();

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

    return PartCreationView;

});