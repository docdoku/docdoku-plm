/*global define*/
define([
        'backbone',
        "mustache",
        "text!templates/part_creation_view.html",
        "common-objects/models/part",
        "collections/part_templates",
        "common-objects/views/attributes/attributes",
        "common-objects/views/workflow/workflow_list",
        "common-objects/views/workflow/workflow_mapping",
        "common-objects/views/security/acl"
    ],
    function (Backbone, Mustache, template, Part, PartTemplateCollection, AttributesView, DocumentWorkflowListView, DocumentWorkflowMappingView, ACLView) {

        var PartCreationView = Backbone.View.extend({

            events: {
                "submit #part_creation_form": "onSubmitForm",
                "hidden #part_creation_modal": "onHidden",
                "change select#inputPartTemplate": "onChangeTemplate"
            },

            initialize: function () {
                _.bindAll(this);
            },

            render: function () {
                this.$el.html(Mustache.render(template, {i18n: APP_CONFIG.i18n}));
                this.bindDomElements();
                this.bindPartTemplateSelector();
                this.bindAttributesView();
                this.$(".tabs").tabs();

                this.workflowsView = new DocumentWorkflowListView({
                    el: this.$("#workflows-list")
                });

                this.workflowsMappingView = new DocumentWorkflowMappingView({
                    el: this.$("#workflows-mapping")
                });

                this.workflowsView.on("workflow:change", this.workflowsMappingView.updateMapping);

                this.workspaceMembershipsView = new ACLView({
                    el: this.$("#acl-mapping"),
                    editMode: true
                }).render();

                return this;
            },

            bindDomElements: function () {
                this.$modal = this.$('#part_creation_modal');
                this.$inputPartTemplate = this.$('#inputPartTemplate');
                this.$inputPartNumber = this.$('#inputPartNumber');
                this.$inputPartName = this.$('#inputPartName');
                this.$inputPartDescription = this.$('#inputPartDescription');
                this.$inputPartStandard = this.$('#inputPartStandard');
            },

            bindPartTemplateSelector: function () {
                this.templateCollection = new PartTemplateCollection();
                this.listenTo(this.templateCollection, "reset", this.onTemplateCollectionReset);
                this.templateCollection.fetch({reset: true});
            },

            bindAttributesView: function () {
                this.attributesView = new AttributesView({
                    el: this.$("#tab-attributes")
                }).render();
            },

            addAttributes: function (template) {
                var that = this;

                this.attributesView.setAttributesLocked(template.isAttributesLocked());

                _.each(template.get("attributeTemplates"), function (object) {
                    that.attributesView.collection.add({
                        name: object.name,
                        type: object.attributeType,
                        mandatory: object.mandatory,
                        value: ""
                    });
                });
            },

            onSubmitForm: function (e) {
                this.model = new Part({
                    number: this.$inputPartNumber.val(),
                    workspaceId: APP_CONFIG.workspaceId,
                    description: this.$inputPartDescription.val(),
                    name: this.$inputPartName.val(),
                    standardPart: this.$inputPartStandard.is(':checked') ? 1 : 0
                });

                var templateId = this.$inputPartTemplate.val();
                var workflow = this.workflowsView.selected();
                var saveOptions = {
                    templateId: templateId ? templateId : null,
                    workflowModelId: workflow ? workflow.get("id") : null,
                    roleMapping: workflow ? this.workflowsMappingView.toList() : null,
                    acl: this.workspaceMembershipsView.toList()
                };

                this.model.save(saveOptions, {
                    wait: true,
                    success: this.onPartCreated,
                    error: this.onError
                });

                e.preventDefault();
                e.stopPropagation();
                return false;
            },

            onPartCreated: function () {
                var that = this;

                this.model.getLastIteration().save({instanceAttributes: this.attributesView.collection.toJSON()}, {
                    success: function () {
                        that.closeModal();
                        that.model.fetch({
                            success: function (model) {
                                that.trigger('part:created', model);
                            }
                        });
                    }
                });
            },

            onError: function (model, error) {
                alert(APP_CONFIG.i18n.CREATION_ERROR + " : " + error.responseText);
            },

            openModal: function () {
                this.$modal.modal('show');
            },

            closeModal: function () {
                this.$modal.modal('hide');
            },

            onHidden: function () {
                this.remove();
            },

            onChangeTemplate: function (e) {

                this.resetMask();
                this.bindAttributesView();


                var templateId = this.$inputPartTemplate.val();

                if (templateId) {
                    var template = this.templateCollection.get(templateId);

                    if (template.get("mask")) {
                        this.setMask(template);
                    }

                    if (template.get("idGenerated")) {
                        this.generate_id(template);
                    }

                    if (template.get("attributeTemplates")) {
                        this.addAttributes(template);
                    }

                }
            },

            resetMask: function () {
                this.$inputPartNumber.unmask(this.mask).val("");
            },

            setMask: function (template) {
                this.mask = template.get("mask");
                this.$inputPartNumber.mask(this.mask);
            },

            onTemplateCollectionReset: function () {
                var that = this;
                this.templateCollection.each(function (model) {
                    that.$inputPartTemplate.append("<option value='" + model.get("id") + "'>" + model.get("id") + "</option>");
                });
            },

            generate_id: function (template) {
                var that = this;
                // Set field mask
                $.getJSON(template.generateIdUrl(), function (data) {
                    if (data) {
                        that.$inputPartNumber.val(data.id);
                    }
                }, "html");
            }

        });

        return PartCreationView;

    });