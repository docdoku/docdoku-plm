define([
        "text!templates/change-issues/change_issue_creation.html",
        "i18n!localization/nls/change-management-strings",
        "models/change_issue",
        "common-objects/collections/users"
    ],
    function (template, i18n, ChangeIssueModel ,UserList) {

    var ChangeIssueCreationView = Backbone.View.extend({
        model: new ChangeIssueModel(),

        events: {
            "submit #issue_creation_form" : "onSubmitForm",
            "hidden #issue_creation_modal": "onHidden"
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
            this.fillPriorityList();
            this.fillCategoryList();
            this.linkManagement();
            return this;
        },

        fillUserList:function(list){
            var self = this ;
            list.each(function(user){
                self.$inputIssueAssignee.append("<option value='"+user.get("login")+"'"+">"+user.get("name")+"</option>");
            });
        },
        fillPriorityList:function(){
            for(var priority in this.model.priorities){
                this.$inputIssuePriority.append("<option value='"+priority+"'"+">"+priority+"</option>");
            }
        },
        fillCategoryList:function(){
            for(var category in this.model.categories){
                this.$inputIssueCategory.append("<option value='"+category+"'"+">"+category+"</option>");
            }
        },

        linkManagement: function(){
            var that = this;
            var $affectedDocumentsLinkZone = this.$("#documents-affected-links");
            require(["common-objects/views/linked/linked_documents","common-objects/collections/linked/linked_document_collection"],
                function(LinkedDocumentsView,LinkedDocumentCollection){
                    that._affectedDocumentsCollection = new LinkedDocumentCollection();
                    var linkedDocumentsView = new LinkedDocumentsView({
                        editMode: true,
                        collection:that._affectedDocumentsCollection
                    }).render();

                    that._subViews.push(linkedDocumentsView);
                    $affectedDocumentsLinkZone.html(linkedDocumentsView.el);
                });
            var $affectedPartsLinkZone = this.$("#parts-affected-links");
            require(["common-objects/views/linked/linked_parts","common-objects/collections/linked/linked_part_collection"],
                function(LinkedPartsView,LinkedPartCollection){
                    that._affectedPartsCollection = new LinkedPartCollection();
                    var linkedPartsView = new LinkedPartsView({
                        editMode: true,
                        collection:that._affectedPartsCollection
                    }).render();

                    that._subViews.push(linkedPartsView);
                    $affectedPartsLinkZone.html(linkedPartsView.el);
                });
        },

        bindDomElements:function(){
            this.$modal = this.$('#issue_creation_modal');
            this.$inputIssueName = this.$('#inputIssueName');
            this.$inputIssueDescription = this.$('#inputIssueDescription');
            this.$inputIssuePriority = this.$('#inputIssuePriority');
            this.$inputIssueAssignee = this.$('#inputIssueAssignee');
            this.$inputIssueCategory = this.$('#inputIssueCategory');
        },

        onSubmitForm: function(e) {
            var data ={
                name: this.$inputIssueName.val(),
                description: this.$inputIssueDescription.val(),
                author:APP_CONFIG.login,
                assignee:this.$inputIssueAssignee.val(),
                priority:this.$inputIssuePriority.val(),
                category:this.$inputIssueCategory.val(),
                initiator:APP_CONFIG.login
            };

            this.model.save(data,{
                success: this.onIssueCreated,
                error: this.error,
                wait: true
            });

            e.preventDefault();
            e.stopPropagation();
            return false ;
        },

        onIssueCreated: function(model){
            this.collection.push(model);
            this.updateAffectedDocuments(model);
            this.updateAffectedParts(model);
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
        }
    });

    return ChangeIssueCreationView;
});