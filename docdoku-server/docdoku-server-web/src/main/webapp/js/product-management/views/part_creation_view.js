define(
    [
        "text!templates/part_creation_view.html",
        "i18n!localization/nls/product-creation-strings",
        "common-objects/models/part",
        "collections/part_templates",
        "common-objects/views/attributes/attributes",
        "common-objects/views/workflow/workflow_list",
        "common-objects/views/workflow/workflow_mapping",
        "common-objects/views/security/acl"
    ],
    function (template, i18n, Part, PartTemplateCollection, AttributesView,DocumentWorkflowListView,DocumentWorkflowMappingView,ACLView) {

    var PartCreationView = Backbone.View.extend({

        events: {
            "submit #part_creation_form" : "onSubmitForm",
            "hidden #part_creation_modal": "onHidden",
            "change select#inputPartTemplate":"onChangeTemplate"
        },

        template: Mustache.compile(template),

        initialize: function() {
            _.bindAll(this);
        },

        render: function() {
            this.$el.html(this.template({i18n: i18n}));
            this.bindDomElements();
            this.bindPartTemplateSelector();
            this.bindAttributesView();
            this.$(".tabs").tabs();

            this.workflowsView = new DocumentWorkflowListView({
                el: this.$("#workflows-list")
            });

            this.workflowsMappingView =  new DocumentWorkflowMappingView({
                el: this.$("#workflows-mapping")
            });

            this.workflowsView.on("workflow:change",this.workflowsMappingView.updateMapping);

            this.workspaceMembershipsView = new ACLView({
                el: this.$("#acl-mapping"),
                editMode:true
            }).render();

            return this;
        },

        bindDomElements:function(){
            this.$modal = this.$('#part_creation_modal');
            this.$inputPartTemplate = this.$('#inputPartTemplate');
            this.$inputPartNumber = this.$('#inputPartNumber');
            this.$inputPartName = this.$('#inputPartName');
            this.$inputPartDescription = this.$('#inputPartDescription');
        },

        bindPartTemplateSelector:function(){
            this.templateCollection = new PartTemplateCollection();
            this.listenTo(this.templateCollection,"reset",this.onTemplateCollectionReset);
            this.templateCollection.fetch({reset:true});
        },

        bindAttributesView:function(){
            this.attributesView = new AttributesView({
                    el: this.$("#tab-attributes")
            }).render();
        },

        onSubmitForm: function(e) {
            this.model = new Part({
                number: this.$inputPartNumber.val(),
                workspaceId: APP_CONFIG.workspaceId,
                description: this.$inputPartDescription.val(),
                name:  this.$inputPartName.val()
            });

            var templateId = this.$inputPartTemplate.val();
            var workflow = this.workflowsView.selected();
            var saveOptions = {
                templateId: templateId ? templateId : null,
                workflowModelId: workflow ? workflow.get("id") : null,
                roleMapping: workflow? this.workflowsMappingView.toList(): null,
                acl:this.workspaceMembershipsView.toList()
            };

            this.model.save(saveOptions, {
                wait: true,
                success: this.onPartCreated,
                error: this.onError
            });

            e.preventDefault();
            e.stopPropagation();
            return false ;
        },

        onPartCreated: function() {
            this.model.getLastIteration().save({instanceAttributes: this.attributesView.collection.toJSON()});
            this.trigger('part:created', this.model);
            this.closeModal();
        },

        onError: function(model, error) {
            alert(i18n.CREATION_ERROR + " : " + error.responseText);
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

        onChangeTemplate:function(e){

            this.resetMask();
            this.resetAttributes();


            var templateId = this.$inputPartTemplate.val();

            if(templateId){
                var template = this.templateCollection.get(templateId);

                if(template.get("mask")){
                    this.setMask(template);
                }

                if (template.get("idGenerated")) {
                    this.generate_id(template);
                }

                if(template.get("attributeTemplates")){
                    this.addAttributes(template);
                }

            }
        },

        resetMask:function(){
            this.$inputPartNumber.unmask(this.mask).val("");
        },

        setMask:function(template){
            this.mask = template.get("mask");
            this.$inputPartNumber.mask(this.mask);
        },

        resetAttributes:function(){
            this.attributesView = new AttributesView({
                el: this.$("#tab-attributes")
            }).render();
        },

        addAttributes:function(template){
            var that = this ;
            _.each(template.get("attributeTemplates"),function(object){
                that.attributesView.collection.add({
                    name: object.name,
                    type: "TEXT",
                    value: ""
                });
            });
        },

        onTemplateCollectionReset:function(){
            var that = this;
            this.templateCollection.each(function(model){
                that.$inputPartTemplate.append("<option value='"+model.get("id")+"'>"+model.get("id")+"</option>");
            });
        },

        generate_id: function (template) {
            var that = this;
            // Set field mask
            $.get(template.generateIdUrl() , function (data) {
                if (data) {
                    that.$inputPartNumber.val(data);
                }
            }, "html");
        }

    });

    return PartCreationView;

});