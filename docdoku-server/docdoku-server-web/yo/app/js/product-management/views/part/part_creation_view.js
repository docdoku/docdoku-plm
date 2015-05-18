/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/part/part_creation_view.html',
    'common-objects/models/part',
    'common-objects/models/tag',
    'collections/part_templates',
    'common-objects/views/attributes/attributes',
    'common-objects/views/workflow/workflow_list',
    'common-objects/views/workflow/workflow_mapping',
    'common-objects/views/security/acl',
    'common-objects/views/alert'
], function (Backbone, Mustache, template, Part, Tag, PartTemplateCollection, AttributesView, WorkflowListView, DocumentWorkflowMappingView, ACLView, AlertView) {
    'use strict';
    var PartCreationView = Backbone.View.extend({

        events: {
            'click .modal-footer .btn-primary': 'interceptSubmit',
            'submit #part_creation_form': 'onSubmitForm',
            'change select#inputPartTemplate': 'onChangeTemplate'
        },

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.bindDomElements();
            this.bindPartTemplateSelector();
            this.bindAttributesView();

            this.workflowsView = new WorkflowListView({
                el: this.$('#workflows-list')
            });

            this.workflowsMappingView = new DocumentWorkflowMappingView({
                el: this.$('#workflows-mapping')
            });

            this.workflowsView.on('workflow:change', this.workflowsMappingView.updateMapping);

            this.workspaceMembershipsView = new ACLView({
                el: this.$('#acl-mapping'),
                editMode: true
            }).render();

            this.attributesView.render();

            this.$inputPartNumber.customValidity(App.config.i18n.REQUIRED_FIELD);

            return this;
        },

        bindDomElements: function () {
            this.$notifications = this.$el.find('.notifications').first();
            this.$inputPartTemplate = this.$('#inputPartTemplate');
            this.$inputPartNumber = this.$('#inputPartNumber');
            this.$inputPartName = this.$('#inputPartName');
            this.$inputPartDescription = this.$('#inputPartDescription');
            this.$inputPartStandard = this.$('#inputPartStandard');
        },

        bindPartTemplateSelector: function () {
            this.templateCollection = new PartTemplateCollection();
            this.listenTo(this.templateCollection, 'reset', this.onTemplateCollectionReset);
            this.templateCollection.fetch({reset: true});
        },

        bindAttributesView: function () {
            this.attributesView = new AttributesView({
                el: this.$('#tab-attributes')
            });
        },

        setWorkflowModel: function(template) {
            this.workflowsView.setValue(template.get('workflowModelId'));
        },

        addAttributes: function (template) {
            var that = this;

            this.attributesView.setAttributesLocked(template.isAttributesLocked());

            _.each(template.get('attributeTemplates'), function (object) {
                that.attributesView.collection.add({
                    name: object.name,
                    type: object.attributeType,
                    mandatory: object.mandatory,
                    locked: object.locked,
                    value: '',
                    lovName:object.lovName
                });
            });

            this.attributesView.render();
        },

        interceptSubmit:function(){
            this.isValid = !this.$('.tabs').invalidFormTabSwitcher();
        },

        onSubmitForm: function (e) {

            if(this.isValid){
                this.model = new Part({
                    number: this.$inputPartNumber.val(),
                    workspaceId: App.config.workspaceId,
                    description: this.$inputPartDescription.val(),
                    name: this.$inputPartName.val(),
                    standardPart: this.$inputPartStandard.is(':checked') ? 1 : 0
                });

                var templateId = this.$inputPartTemplate.val();
                var workflow = this.workflowsView.selected();
                var saveOptions = {
                    templateId: templateId ? templateId : null,
                    workflowModelId: workflow ? workflow.get('id') : null,
                    roleMapping: workflow ? this.workflowsMappingView.toList() : null,
                    acl: this.workspaceMembershipsView.toList()
                };

                this.model.save(saveOptions, {
                    wait: true,
                    success: this.onPartCreated,
                    error: this.onError
                });
            }

            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        onPartCreated: function () {
            var that = this;

            this.model.getLastIteration().save({
                instanceAttributes: this.attributesView.collection.toJSON()
            }, {
                success: function () {
                    if (that.options.autoAddTag) {
                        var tag = new Tag({
                            label: that.options.autoAddTag,
                            id: that.options.autoAddTag,
                            workspaceId: App.config.workspaceId
                        });
                        that.model.addTags([tag]).success(function() {
                            that.closeModal();
                            that.model.fetch({
                                success: function (model) {
                                    that.trigger('part:created', model);
                                }
                            });
                        });
                    } else {
                        that.closeModal();
                        that.model.fetch({
                            success: function (model) {
                                that.trigger('part:created', model);
                            }
                        });
                    }
                }
            });
        },

        onError: function (model, error) {
            var errorMessage = error ? error.responseText : model;

            this.$notifications.append(new AlertView({
                type: 'error',
                message: errorMessage
            }).render().$el);
        },

        openModal: function () {
            this.$el.one('shown', this.render.bind(this));
            this.$el.one('hidden', this.onHidden.bind(this));
            this.$el.modal('show');
        },

        closeModal: function () {
            this.$el.modal('hide');
        },

        onHidden: function () {
            this.remove();
        },

        onChangeTemplate: function () {
            this.resetMask();
            this.bindAttributesView();

            var templateId = this.$inputPartTemplate.val();

            if (templateId) {
                var template = this.templateCollection.get(templateId);

                if (template.get('mask')) {
                    this.setMask(template);
                }

                if (template.get('idGenerated')) {
                    this.generateId(template);
                }

                if (template.get('workflowModelId')) {
                    this.setWorkflowModel(template);
                }

                if (template.get('attributeTemplates')) {
                    this.addAttributes(template);
                }
            }
        },

        resetMask: function () {
            this.$inputPartNumber.unmask(this.mask).val('');
        },

        setMask: function (template) {
            this.mask = template.get('mask');
            this.$inputPartNumber.mask(this.mask);
        },

        onTemplateCollectionReset: function () {
            var that = this;
            this.templateCollection.each(function (model) {
                that.$inputPartTemplate.append('<option value="' + model.get('id') + '">' + model.get('id') + '</option>');
            });
        },

        generateId: function (template) {
            var that = this;
            // Set field mask
            $.getJSON(template.generateIdUrl(), function (data) {
                if (data) {
                    that.$inputPartNumber.val(data.id);
                }
            }, 'html');
        }

    });

    return PartCreationView;

});
