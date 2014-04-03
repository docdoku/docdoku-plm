define([
        "text!templates/milestones/milestone_edition.html",
        "i18n!localization/nls/change-management-strings"
],
    function (template, i18n) {

    var MilestoneEditionView = Backbone.View.extend({
        events: {
            "submit #milestone_edition_form" : "onSubmitForm",
            "hidden #milestone_edition_modal": "onHidden"
        },

        template: Mustache.compile(template),

        initialize: function() {
            this._subViews = [];
            this.model.fetch();
            _.bindAll(this);
            this.$el.on("remove",this.removeSubviews);                                                                  // Remove cascade
        },

        removeSubviews: function(){
            _(this._subViews).invoke("remove");
        },

        render: function() {
            var hasRequest = this.model.getNumberOfRequests()> 0;
            var hasOrder = this.model.getNumberOfOrders()> 0;
            this.removeSubviews();
            this.editMode = this.model.isWritable();
            this.$el.html(this.template({i18n: i18n, hasRequest: hasRequest, hasOrder: hasOrder, model: this.model}));
            this.bindDomElements();
            this.initValue();
            this.linkManagement();
            return this;
        },

        bindDomElements:function(){
            this.$modal = this.$('#milestone_edition_modal');
            this.$inputMilestoneTitle = this.$('#inputMilestoneTitle');
            this.$inputMilestoneDescription = this.$('#inputMilestoneDescription');
            this.$inputMilestoneDueDate = this.$('#inputMilestoneDueDate');
        },

        initValue: function () {
            this.$inputMilestoneTitle.val(this.model.getTitle());
            this.$inputMilestoneDueDate.val(this.model.getDueDateToPrint());
            this.$inputMilestoneDescription.val(this.model.getDescription());
        },

        linkManagement: function(){
            var that = this;
            var $affectedRequestsLinkZone = this.$("#requests-affected-links");
            require(["common-objects/views/linked/linked_requests","common-objects/collections/linked/linked_change_item_collection"],
            function(LinkedRequestsView,LinkedChangeItemCollection){
                var affectedRequestsCollection = new LinkedChangeItemCollection();
                affectedRequestsCollection.url=that.model.url()+"/requests";
                affectedRequestsCollection.fetch({
                    success: function(){
                        var linkedRequestsView = new LinkedRequestsView({
                            editMode: false,
                            collection:affectedRequestsCollection
                        }).render();

                        that._subViews.push(linkedRequestsView);
                        $affectedRequestsLinkZone.html(linkedRequestsView.el);
                    }
                });
            });
            var $affectedOrdersLinkZone = this.$("#orders-affected-links");
            require(["common-objects/views/linked/linked_orders","common-objects/collections/linked/linked_change_item_collection"],
                function(LinkedOrdersView,LinkedChangeItemCollection){
                    var affectedOrdersCollection = new LinkedChangeItemCollection();
                    affectedOrdersCollection.url=that.model.url()+"/orders";
                    affectedOrdersCollection.fetch({
                        success: function(){
                            var linkedOrdersView = new LinkedOrdersView({
                                editMode: false,
                                collection:affectedOrdersCollection
                            }).render();

                            that._subViews.push(linkedOrdersView);
                            $affectedOrdersLinkZone.html(linkedOrdersView.el);
                        }
                    });
                });
        },

        onSubmitForm: function(e) {
            var data ={
                title: this.$inputMilestoneTitle.val(),
                description: this.$inputMilestoneDescription.val(),
                dueDate: this.$inputMilestoneDueDate.val()+"T00:00:00"
            };

            this.model.save(data,{
                success: this.closeModal,
                error: this.onError,
                wait: true
            });

            e.preventDefault();
            e.stopPropagation();
            return false ;
        },

        onError: function(model, error){
            alert(i18n.EDITION_ERROR + " : " + error.responseText);
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

    return MilestoneEditionView;
});