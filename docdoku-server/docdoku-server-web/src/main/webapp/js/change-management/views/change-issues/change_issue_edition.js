define([
        "text!templates/change-issues/change_issue_edition.html",
        "i18n!localization/nls/change-management-strings",
        "common-objects/collections/users"
    ],
    function (template, i18n, UserList) {

    var ChangeIssueEditionView = Backbone.View.extend({
        events: {
            "submit #issue_edition_form" : "onSubmitForm",
            "hidden #issue_edition_modal": "onHidden"
        },

        template: Mustache.compile(template),

        initialize: function() {
            this.tagsToRemove = [];
            this._subViews = [];
            this.model.fetch();
            _.bindAll(this);
            this.$el.on("remove",this.removeSubviews);                                                                  // Remove cascade
        },

        removeSubviews: function(){
            _(this._subViews).invoke("remove");
        },

        render: function() {
            this.removeSubviews();
            this.editMode = this.model.isWritable();
            this.$el.html(this.template({i18n: i18n, model: this.model}));
            this.bindDomElements();
            new UserList().fetch({success: this.fillUserList});
            this.fillPriorityList();
            this.fillCategoryList();
            this.initValue();
            this.tagManagement();
            this.linkManagement();
            return this;
        },

        fillUserList:function(list){
            var self = this ;
            list.each(function(user){
                self.$inputIssueAssignee.append("<option value='"+user.get("login")+"'"+">"+user.get("name")+"</option>");
            });
            this.$inputIssueAssignee.val(this.model.getAssignee());
        },
        fillPriorityList:function(){
            for(var priority in this.model.priorities){
                this.$inputIssuePriority.append("<option value='"+priority+"'"+">"+priority+"</option>")
            }
            this.$inputIssuePriority.val(this.model.getPriority());
        },
        fillCategoryList:function(){
            for(var category in this.model.categories){
                this.$inputIssueCategory.append("<option value='"+category+"'"+">"+category+"</option>")
            }
            this.$inputIssueCategory.val(this.model.getCategory());
        },

        tagManagement: function(){
            var that = this;

            if(this.model.attributes.tags.length){
                require(["common-objects/models/tag",
                         "common-objects/views/tags/tag"],
                function(Tag, TagView){
                    var $tagsZone = this.$(".master-tags-list");
                    _.each(that.model.attributes.tags, function (tagLabel) {
                        var tagView;
                        var tagViewParams = {
                            model: new Tag({id: tagLabel, label: tagLabel}),
                            isAdded: that.editMode,
                            clicked: function () {
                                that.tagsToRemove.push(tagLabel);
                                tagView.$el.remove();
                            }
                        };

                        tagView = new TagView(tagViewParams).render();
                        that._subViews.push(tagView);

                        $tagsZone.append(tagView.el);
                    });
                });
            }
        },

        linkManagement: function(){
            var that = this;
            var affectedDocuments = this.model.getAffectedDocuments();
            var $affectedDocumentsLinkZone = this.$("#documents-affected-links");
            require(["common-objects/views/linked/linked_documents","common-objects/collections/linked/linked_document_collection"],
            function(LinkedDocumentsView,LinkedDocumentCollection){
                that._affectedDocumentsCollection = new LinkedDocumentCollection(affectedDocuments);
                var linkedDocumentsView = new LinkedDocumentsView({
                    editMode: that.editMode,
                    collection:that._affectedDocumentsCollection
                }).render();

                that._subViews.push(linkedDocumentsView);
                $affectedDocumentsLinkZone.html(linkedDocumentsView.el);
            });
            var affectedParts = this.model.getAffectedParts();
            var $affectedPartsLinkZone = this.$("#parts-affected-links");
            require(["common-objects/views/linked/linked_parts","common-objects/collections/linked/linked_part_collection"],
            function(LinkedPartsView,LinkedPartCollection){
                that._affectedPartsCollection = new LinkedPartCollection(affectedParts);
                var linkedPartsView = new LinkedPartsView({
                    editMode: that.editMode,
                    collection:that._affectedPartsCollection
                }).render();

                that._subViews.push(linkedPartsView);
                $affectedPartsLinkZone.html(linkedPartsView.el);
            });
        },

        bindDomElements:function(){
            this.$modal = this.$('#issue_edition_modal');
            this.$inputIssueName = this.$('#inputIssueName');
            this.$inputIssueInitiator = this.$('#inputIssueInitiator');
            this.$inputIssueDescription = this.$('#inputIssueDescription');
            this.$inputIssuePriority = this.$('#inputIssuePriority');
            this.$inputIssueAssignee = this.$('#inputIssueAssignee');
            this.$inputIssueCategory = this.$('#inputIssueCategory');
        },

        initValue: function () {
            this.$inputIssueName.val(this.model.getName());
            this.$inputIssueInitiator.val(this.model.getInitiator());
            this.$inputIssueDescription.val(this.model.getDescription());
        },

        onSubmitForm: function(e) {
            var data ={
                description: this.$inputIssueDescription.val(),
                author:APP_CONFIG.login,
                assignee:this.$inputIssueAssignee.val(),
                priority:this.$inputIssuePriority.val(),
                category:this.$inputIssueCategory.val()
            };

            this.model.save(data,{
                success: this.closeModal,
                error: this.error,
                wait: true
            });

            this.deleteClickedTags();                                                                                   // Delete tags if needed
            this.updateAffectedDocuments();
            this.updateAffectedParts();

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
        },

        deleteClickedTags: function(){
            if(this.tagsToRemove.length){
                var that = this ;
                this.model.removeTags(this.tagsToRemove, function(){
                    if(that.model.collection.parent) {
                        if(_.contains(that.tagsToRemove, that.model.collection.parent.id)){
                            that.model.collection.remove(that.model);
                        }
                    }
                });
            }
        },

        updateAffectedDocuments: function () {
            this.model.saveAffectedDocuments(this._affectedDocumentsCollection);
        },

        updateAffectedParts: function () {
            this.model.saveAffectedParts(this._affectedPartsCollection);
        }
    });

    return ChangeIssueEditionView;
});