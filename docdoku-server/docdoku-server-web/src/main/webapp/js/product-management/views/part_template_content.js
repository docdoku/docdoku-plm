define([
    "collections/part_templates",
    "text!templates/part_template_content.html",
    "i18n!localization/nls/product-management-strings",
    "views/part_template_list",
    "views/part_template_creation_view"
], function (
    PartTemplateCollection,
    template,
    i18n,
    PartTemplateListView,
    PartTemplateCreationView
    ) {
    var PartTemplateContentView = Backbone.View.extend({

        template: Mustache.compile(template),

        el: "#product-management-content",

        events:{
            "click button.new-template":"newPartTemplate",
            "click button.delete-part-template":"deletePartTemplate"
        },

        initialize: function () {
            _.bindAll(this);
        },

        render:function(){
            this.$el.html(this.template({i18n:i18n}));

            this.bindDomElements();

            this.partTemplateListView = new PartTemplateListView({
                el:this.$("#part_template_table"),
                collection:new PartTemplateCollection()
            }).render();

            this.partTemplateListView.on("delete-button:display", this.changeDeleteButtonDisplay);
            return this;
        },

        bindDomElements:function(){
            this.deleteButton = this.$(".delete");
        },

        newPartTemplate:function(e){
            var partTemplateCreationView = new PartTemplateCreationView().render();
            this.listenTo(partTemplateCreationView, 'part-template:created', this.fetchPartTemplateAndAdd);
            partTemplateCreationView.show();
        },

        fetchPartTemplateAndAdd:function(partTemplate){
            this.addPartTemplateInList(partTemplate);
        },

        deletePartTemplate:function(){
            this.partTemplateListView.deleteSelectedPartTemplates();
        },

        addPartTemplateInList:function(partTemplate){
            this.partTemplateListView.pushPartTemplate(partTemplate);
        },

        changeDeleteButtonDisplay:function(state){
            if(state){
                this.deleteButton.show();
            }else{
                this.deleteButton.hide();
            }
        }

    });

    return PartTemplateContentView;

});
