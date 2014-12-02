/*global define*/
define([
    'backbone',
    "mustache",
    "collections/part_templates",
    "text!templates/part_template_content.html",
    "views/part_template_list",
    "views/part_template_creation_view",
    "text!common-objects/templates/buttons/delete_button.html"
], function (Backbone, Mustache, PartTemplateCollection, template, PartTemplateListView, PartTemplateCreationView, deleteButton) {
    var PartTemplateContentView = Backbone.View.extend({

        el: "#product-management-content",

        partials: {
            deleteButton: deleteButton
        },

        events: {
            "click button.new-template": "newPartTemplate",
            "click button.delete": "deletePartTemplate"
        },

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}, this.partials));

            this.bindDomElements();

            this.partTemplateListView = new PartTemplateListView({
                el: this.$("#part_template_table"),
                collection: new PartTemplateCollection()
            }).render();

            this.partTemplateListView.on("delete-button:display", this.changeDeleteButtonDisplay);
            return this;
        },

        bindDomElements: function () {
            this.deleteButton = this.$(".delete");
        },

        newPartTemplate: function () {
            var partTemplateCreationView = new PartTemplateCreationView();
            this.listenTo(partTemplateCreationView, 'part-template:created', this.fetchPartTemplateAndAdd);
            partTemplateCreationView.show();
        },

        fetchPartTemplateAndAdd: function (partTemplate) {
            this.addPartTemplateInList(partTemplate);
        },

        deletePartTemplate: function () {
            this.partTemplateListView.deleteSelectedPartTemplates();
        },

        addPartTemplateInList: function (partTemplate) {
            this.partTemplateListView.pushPartTemplate(partTemplate);
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
