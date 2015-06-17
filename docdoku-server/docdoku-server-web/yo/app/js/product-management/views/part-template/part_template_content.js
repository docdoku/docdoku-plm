/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'collections/part_templates',
    'text!templates/part-template/part_template_content.html',
    'views/part-template/part_template_list',
    'views/part-template/part_template_creation_view',
    'common-objects/views/security/acl_edit',
    'text!common-objects/templates/buttons/delete_button.html',
    'text!common-objects/templates/buttons/ACL_button.html',
    'common-objects/views/lov/lov_modal'
], function (Backbone, Mustache, PartTemplateCollection, template, PartTemplateListView, PartTemplateCreationView,ACLEditView, deleteButton,aclButton, LOVModalView) {
    'use strict';
    var PartTemplateContentView = Backbone.View.extend({
        partials: {
            deleteButton: deleteButton,
            aclButton: aclButton
        },

        events: {
            'click button.new-template': 'newPartTemplate',
            'click button.delete': 'deletePartTemplate',
            'click .actions .edit-acl': 'onEditAcl',
            'click .actions .list-lov' : 'showLovs'
        },

        initialize: function () {
            _.bindAll(this);
        },
        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}, this.partials));
            this.bindDomElements();

            if(!this.partTemplateCollection){
                this.partTemplateCollection = new PartTemplateCollection();
            }

            if(this.partTemplateListView){
                this.partTemplateListView.remove();
            }

            this.partTemplateListView = new PartTemplateListView({
                el: this.$('#part_template_table'),
                collection: this.partTemplateCollection
            }).render();

            this.bindEvent();
            return this;
        },
        bindDomElements: function () {
            this.$notifications = this.$el.find('.notifications').first();
            this.deleteButton = this.$('.delete');
            this.aclButton = this.$('.actions .edit-acl');

        },
        bindEvent: function(){
            this.partTemplateListView.on('error', this.onError);
            this.partTemplateListView.on('warning', this.onWarning);
            this.partTemplateListView.on('delete-button:display', this.changeDeleteButtonDisplay);
            this.partTemplateListView.on('acl-button:display', this.changeACLButtonDisplay);
            this.delegateEvents();

        },


        newPartTemplate: function () {
            var partTemplateCreationView = new PartTemplateCreationView();
            partTemplateCreationView.on('part-template:created', this.partTemplateCollection.push, this.partTemplateCollection);
            partTemplateCreationView.show();
        },
        deletePartTemplate: function () {
            this.partTemplateListView.deleteSelectedPartTemplates();
        },


        changeDeleteButtonDisplay: function (state) {
            if (state) {
                this.deleteButton.show();
            } else {
                this.deleteButton.hide();
            }
        },
        changeACLButtonDisplay: function(state){
            if (state) {
                this.aclButton.show();

            } else {
                this.aclButton.hide();
            }
        },
        onEditAcl: function () {
            this.partTemplateListView.editSelectedPartTemplateACL();
        },
        showLovs: function(){
            var lovmodal = new LOVModalView({
            });
            window.document.body.appendChild(lovmodal.render().el);
            lovmodal.openModal();
        }

    });

    return PartTemplateContentView;
});
