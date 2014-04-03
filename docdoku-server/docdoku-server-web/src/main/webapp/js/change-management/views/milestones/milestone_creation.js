define([
        "text!templates/milestones/milestone_creation.html",
        "i18n!localization/nls/change-management-strings",
        "models/milestone"
    ],
    function (template, i18n, MilestoneModel) {

    var MilestoneCreationView = Backbone.View.extend({
        model: new MilestoneModel(),

        events: {
            "submit #milestone_creation_form" : "onSubmitForm",
            "hidden #milestone_creation_modal": "onHidden"
        },

        template: Mustache.compile(template),

        initialize: function() {
            _.bindAll(this);
        },

        render: function() {
            this.$el.html(this.template({i18n: i18n}));
            this.bindDomElements();
            return this;
        },

        bindDomElements:function(){
            this.$modal = this.$('#milestone_creation_modal');
            this.$inputMilestoneTitle = this.$('#inputMilestoneTitle');
            this.$inputMilestoneDescription = this.$('#inputMilestoneDescription');
            this.$inputMilestoneDueDate = this.$('#inputMilestoneDueDate');
        },

        onSubmitForm: function(e) {
            var data ={
                title: this.$inputMilestoneTitle.val(),
                description: this.$inputMilestoneDescription.val(),
                dueDate: this.$inputMilestoneDueDate.val()+"T00:00:00"
            };

            new MilestoneModel().save(data,{
                success: this.onMilestoneCreated,
                error: this.onError,
                wait: true
            });

            e.preventDefault();
            e.stopPropagation();
            return false ;
        },

        onMilestoneCreated: function(model){
            this.collection.push(model);
            this.closeModal();
        },

        onError: function(model, error){
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
        }
    });

    return MilestoneCreationView;
});