define(
    [
        "text!templates/part_creation.html",
        "i18n!localization/nls/product-creation-strings",
        "models/part",
        "commander"
    ],
    function (template, i18n, Part, Commander) {

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

            Commander.getWorkspaces({success:function(workspacesStr){
                var workspaces = JSON.parse(workspacesStr);
                self.$el.html(self.template({i18n : i18n, workspaces:workspaces}));
                self.bindDomElements();
                self.openModal();
            }});

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

            Commander.createPart(part, this.model.getFullPath(), function() {
                self.closeModal();
                self.trigger("part:created",self.model);
            }, function() {
                alert("Error : part not created");
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