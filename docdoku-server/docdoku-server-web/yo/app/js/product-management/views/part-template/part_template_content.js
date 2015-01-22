/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'collections/part_templates',
    'text!templates/part-template/part_template_content.html',
    'views/part-template/part_template_list',
    'views/part-template/part_template_creation_view',
    'text!common-objects/templates/buttons/delete_button.html'
], function (Backbone, Mustache, PartTemplateCollection, template, PartTemplateListView, PartTemplateCreationView, deleteButton) {
    'use strict';
    var PartTemplateContentView = Backbone.View.extend({
        partials: {
            deleteButton: deleteButton
        },

        events: {
            'click button.new-template': 'newPartTemplate',
            'click button.delete': 'deletePartTemplate'
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
        },
        bindEvent: function(){
            this.partTemplateListView.on('error', this.onError);
            this.partTemplateListView.on('warning', this.onWarning);
            this.partTemplateListView.on('delete-button:display', this.changeDeleteButtonDisplay);
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
        }

    });

    return PartTemplateContentView;
});
