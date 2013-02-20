define(["text!modules/product-creation-module/templates/product_creation_view.html", "i18n!localization/nls/product-creation-strings", "modules/product-creation-module/models/configuration_item"], function (template, i18n, ConfigurationItem) {

    var ProductCreationView = Backbone.View.extend({

        events: {
            "hidden #product_creation_modal": "onHidden",
            "submit #product_creation_form" : "onSubmitForm",
            "click #show_part_creation_fields" : "showPartCreationFields",
            "click #hide_part_creation_fields" : "hidePartCreationFields"
        },

        template: Mustache.compile(template),

        initialize: function() {
            _.bindAll(this);
            this.createNewPart = false ;
        },

        render: function() {
            this.$el.html(this.template({i18n: i18n}));
            this.bindDomElements();
            this.bindTypeahead();
            return this;
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

        bindDomElements:function(){
            this.$modal = this.$('#product_creation_modal');
            this.$inputPartNumber = this.$('#inputPartNumber');
            this.$inputProductId = this.$('#inputProductId');
            this.$inputDescription = this.$('#inputDescription');

            this.$showPartCreationFields = this.$('#show_part_creation_fields');
            this.$hidePartCreationFields = this.$('#hide_part_creation_fields');
            this.$partCreationForm = this.$('#part_creation_fields');

            this.$inputNewPartNumber = this.$('#inputNewPartNumber');
            this.$inputNewPartName = this.$('#inputNewPartName');
            this.$inputNewPartDescription = this.$('#inputNewPartDescription');
            this.$inputNewPartStandard = this.$('#inputNewPartStandard');

            this.$inputPartNumberWrapper = this.$("#inputPartNumberWrapper");
        },

        bindTypeahead: function() {
            this.$inputPartNumber.typeahead({
                source: function(query, process) {
                    $.getJSON('/api/workspaces/' + APP_CONFIG.workspaceId + '/parts?q=' + query, function(data) {
                        process(data);
                    });
                }
            });
        },

        onSubmitForm: function(e) {

            var that = this ;

            var partNumber = this.createNewPart ? this.$inputNewPartNumber.val() : this.$inputPartNumber.val() ;

            var saveModel = function(){

                that.model = new ConfigurationItem({
                    id: that.$inputProductId.val(),
                    workspaceId: APP_CONFIG.workspaceId,
                    description: that.$inputDescription.val(),
                    designItemNumber: partNumber
                });

                that.model.save({}, {
                    wait: true,
                    success: that.onProductCreated,
                    error: that.onError
                });
            };

            if(!this.createNewPart){
                saveModel();
            }else{

                var newPart = {
                    number : partNumber,
                    name : this.$inputNewPartName.val(),
                    description : this.$inputNewPartDescription.val(),
                    standardPart : this.$inputNewPartStandard.is(":checked")
                };

                $.ajax({
                    context: this,
                    type: "PUT",
                    url : '/api/workspaces/' + APP_CONFIG.workspaceId + '/parts',
                    data : JSON.stringify(newPart),
                    contentType: "application/json; charset=utf-8",
                    success: saveModel,
                    error: this.onError
                });

            }

            e.preventDefault();
            e.stopPropagation();
            return false ;

        },

        onProductCreated: function() {
            this.trigger('product:created', this.model);
            this.closeModal();
        },

        showPartCreationFields:function(){
            this.$partCreationForm.show();

            this.$inputPartNumberWrapper.hide();

            this.$showPartCreationFields.hide();
            this.$hidePartCreationFields.show();

            this.createNewPart  = true ;

            this.$inputPartNumber.removeProp("required");

            this.$inputNewPartNumber.attr("required","required");
            this.$inputNewPartName.attr("required","required");

        },

        hidePartCreationFields:function(){

            this.$partCreationForm.hide();

            this.$inputPartNumberWrapper.show();

            this.$showPartCreationFields.show();
            this.$hidePartCreationFields.hide();

            this.createNewPart  = false;

            this.$inputPartNumber.attr("required","required");

            this.$inputNewPartNumber.removeProp("required");
            this.$inputNewPartName.removeProp("required");

        },

        onError: function() {
            alert(i18n.CREATION_ERROR);
        }

    });

    return ProductCreationView;

});