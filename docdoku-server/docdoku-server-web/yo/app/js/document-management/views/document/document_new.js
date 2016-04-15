/*global define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/views/components/modal',
    'common-objects/views/attributes/attributes',
    'views/document/document_template_list',
    'common-objects/views/workflow/workflow_list',
    'common-objects/views/workflow/workflow_mapping',
    'common-objects/views/security/acl',
    'text!templates/document/document_new.html'
], function (Backbone, Mustache, ModalView, AttributesView, DocumentTemplateListView, DocumentWorkflowListView, DocumentWorkflowMappingView, ACLView, template) {
    'use strict';
    var DocumentNewView = ModalView.extend({

        template: template,

        initialize: function () {
            ModalView.prototype.initialize.apply(this, arguments);
            this.events['click .modal-footer button.btn-primary'] = 'interceptSubmit';
            this.events['submit #form-' + this.cid] = 'onSubmitForm';
        },

        rendered: function () {

            this.workflowsView = this.addSubView(
                new DocumentWorkflowListView({
                    el: '#workflows-' + this.cid
                })
            );

            this.workflowsMappingView = this.addSubView(
                new DocumentWorkflowMappingView({
                    el: '#workflows-mapping-' + this.cid
                })
            );

            this.workflowsView.on('workflow:change', this.workflowsMappingView.updateMapping);

            this.attributesView = this.addSubView(
                new AttributesView({
                    el: '#tab-attributes-' + this.cid
                })
            );

            this.attributesView.render();

            this.templatesView = this.addSubView(
                new DocumentTemplateListView({
                    el: '#templates-' + this.cid,
                    workflowsView: this.workflowsView,
                    attributesView: this.attributesView
                })
            );

            this.templatesView.collection.fetch({reset: true});

            this.workspaceMembershipsView = new ACLView({
                el: this.$('#acl-mapping-' + this.cid),
                editMode: true
            }).render();

            this.$('input.reference').customValidity(App.config.i18n.REQUIRED_FIELD);

        },

        interceptSubmit:function(){
            this.isValid = !this.$('.tabs').invalidFormTabSwitcher();
        },

        onSubmitForm: function () {

            if (this.isValid) {
                var workflow = this.workflowsView.selected();
                var template = this.templatesView.selected();
                var acl = this.workspaceMembershipsView.toList();

                var data = {
                    reference: this.$('#form-' + this.cid + ' .reference').val(),
                    title: this.$('#form-' + this.cid + ' .title').val(),
                    description: this.$('#form-' + this.cid + ' .description').val(),
                    workflowModelId: workflow ? workflow.get('id') : null,
                    templateId: template ? template.get('id') : null,
                    roleMapping: workflow ? this.workflowsMappingView.toList() : null,
                    acl: acl
                };



                this.collection.create(data, {
                    success: this.success,
                    error: this.error,
                    wait: true
                });
            }

            return false;
        },

        success: function (model) {
            var that = this;
            model.getLastIteration().save({
                instanceAttributes: this.attributesView.collection.toJSON()
            }, {
                success: function () {
                    if(that.options.autoAddTag){
                        model.addTags([that.options.autoAddTag]).success(function(){
                            that.hide();
                            model.fetch();
                        });
                    }else{
                        that.hide();
                        model.fetch();
                    }
                    Backbone.Events.trigger('document:iterationChange');
                },
                error: this.error
            });
        },

        error: function (model, error) {
            this.collection.remove(model);
            if (error.responseText) {
                this.alert({
                    type: 'error',
                    message: error.responseText
                });
            } else {
                console.error(error);
            }
        }

    });

    return DocumentNewView;

});
