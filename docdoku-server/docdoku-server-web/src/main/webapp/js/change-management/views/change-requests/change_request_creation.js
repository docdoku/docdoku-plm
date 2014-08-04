define([
        "text!templates/change-requests/change_request_creation.html",
        "i18n!localization/nls/change-management-strings",
        "models/change_request",
        "common-objects/collections/users",
        "collections/milestone_collection"
    ],
    function (template, i18n, ChangeRequestModel ,UserList, MilestoneList) {

    var ChangeRequestCreationView = Backbone.View.extend({
        model: new ChangeRequestModel(),

        events: {
            "submit #request_creation_form" : "onSubmitForm",
            "hidden #request_creation_modal": "onHidden"
        },

        template: Mustache.compile(template),

        initialize: function() {
            this._subViews = [];
            _.bindAll(this);
            this.$el.on("remove",this.removeSubviews);                                                                  // Remove cascade
        },

        removeSubviews: function(){
            _(this._subViews).invoke("remove");
        },

        render: function() {
            this.$el.html(this.template({i18n: i18n}));
            this.bindDomElements();
            new UserList().fetch({success: this.fillUserList});
            new MilestoneList().fetch({success: this.fillMilestoneList});
            this.fillPriorityList();
            this.fillCategoryList();
            this.linkManagement();
            return this;
        },

        fillMilestoneList:function(list){
            var self = this ;
            if(list){
                list.each(function(milestone){
                    self.$inputRequestMilestone.append("<option value='"+milestone.get("id")+"'"+">"+milestone.get("title")+"</option>");
                });
            }
        },
        fillUserList:function(list){
            var self = this ;
            if(list){
                list.each(function(user){
                    self.$inputRequestAssignee.append("<option value='"+user.get("login")+"'"+">"+user.get("name")+"</option>");
                });
            }
        },
        fillPriorityList:function(){
            for(var priority in this.model.priorities){
                this.$inputRequestPriority.append("<option value='"+priority+"'"+">"+priority+"</option>");
            }
        },
        fillCategoryList:function(){
            for(var category in this.model.categories){
                this.$inputRequestCategory.append("<option value='"+category+"'"+">"+category+"</option>");
            }
        },

        linkManagement: function(){
            var that = this;
            var $affectedDocumentsLinkZone = this.$("#documents-affected-links");
            require(["common-objects/views/linked/linked_documents","common-objects/collections/linked/linked_document_collection"],
                function(LinkedDocumentsView,LinkedDocumentCollection){
                    that._affectedDocumentsCollection = new LinkedDocumentCollection();
                    that._linkedDocumentsView = new LinkedDocumentsView({
                        editMode: true,
                        collection:that._affectedDocumentsCollection
                    }).render();

                    that._subViews.push(that._linkedDocumentsView);
                    $affectedDocumentsLinkZone.html(that._linkedDocumentsView.el);
                });
            var $affectedPartsLinkZone = this.$("#parts-affected-links");
            require(["common-objects/views/linked/linked_parts","common-objects/collections/linked/linked_part_collection"],
                function(LinkedPartsView,LinkedPartCollection){
                    that._affectedPartsCollection = new LinkedPartCollection();
                    that._linkedPartsView = new LinkedPartsView({
                        editMode: true,
                        collection:that._affectedPartsCollection
                    }).render();

                    that._subViews.push(that._linkedPartsView);
                    $affectedPartsLinkZone.html(that._linkedPartsView.el);
                });
            var $affectedIssuesLinkZone = this.$("#issues-affected-links");
            require(["common-objects/views/linked/linked_issues","common-objects/collections/linked/linked_change_item_collection"],
                function(LinkedIssuesView,LinkedChangeItemCollection){
                    that._affectedIssuesCollection = new LinkedChangeItemCollection();
                    var linkedIssuesView = new LinkedIssuesView({
                        editMode: true,
                        collection:that._affectedIssuesCollection,
                        linkedPartsView: that._linkedPartsView,
                        linkedDocumentsView: that._linkedDocumentsView
                    }).render();

                    that._subViews.push(linkedIssuesView);
                    $affectedIssuesLinkZone.html(linkedIssuesView.el);
                });
        },

        bindDomElements:function(){
            this.$modal = this.$('#request_creation_modal');
            this.$inputRequestName = this.$('#inputRequestName');
            this.$inputRequestDescription = this.$('#inputRequestDescription');
            this.$inputRequestMilestone = this.$('#inputRequestMilestone');
            this.$inputRequestPriority = this.$('#inputRequestPriority');
            this.$inputRequestAssignee = this.$('#inputRequestAssignee');
            this.$inputRequestCategory = this.$('#inputRequestCategory');
        },

        onSubmitForm: function(e) {
            var data ={
                name: this.$inputRequestName.val(),
                description: this.$inputRequestDescription.val(),
                author:APP_CONFIG.login,
                assignee:this.$inputRequestAssignee.val(),
                priority:this.$inputRequestPriority.val(),
                category:this.$inputRequestCategory.val(),
                milestoneId:parseInt(this.$inputRequestMilestone.val(),10)
            };

            this.model.save(data,{
                success: this.onRequestCreated,
                error: this.error,
                wait: true
            });

            e.preventDefault();
            e.stopPropagation();
            return false ;
        },

        onRequestCreated: function(model){
            this.collection.push(model);
            this.updateAffectedDocuments(model);
            this.updateAffectedParts(model);
            this.updateAffectedIssues(model);
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
        },

        updateAffectedDocuments: function (model) {
            if(this._affectedDocumentsCollection.length){
                model.saveAffectedDocuments(this._affectedDocumentsCollection);
            }
        },

        updateAffectedParts: function (model) {
            if(this._affectedPartsCollection.length){
                model.saveAffectedParts(this._affectedPartsCollection);
            }
        },

        updateAffectedIssues: function (model) {
            if(this._affectedIssuesCollection.length){
                model.saveAffectedIssues(this._affectedIssuesCollection);
            }
        }
    });

    return ChangeRequestCreationView;
});