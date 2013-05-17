define([
    "common-objects/collections/part_collection",
    "text!templates/part_content.html",
    "i18n!localization/nls/product-management-strings",
    "views/part_list",
    "views/part_creation_view",
    "common-objects/views/prompt",
    "common-objects/views/security/acl_edit"
], function (
    PartCollection,
    template,
    i18n,
    PartListView,
    PartCreationView,
    PromptView,
    ACLEditView
    ) {
    var PartContentView = Backbone.View.extend({

        template: Mustache.compile(template),

        el: "#product-management-content",

        events:{
            "click button.new-part":"newPart",
            "click button.delete-part":"deletePart",
            "click button.checkout":"checkout",
            "click button.undocheckout":"undocheckout",
            "click button.checkin":"checkin",
            "click button.edit-acl":"updateACL",
            "click button.next-page":"toNextPage",
            "click button.previous-page":"toPreviousPage",
            "click button.first-page":"toFirstPage",
            "click button.last-page":"toLastPage",
            "click button.current-page":"goToPage"
        },

        initialize: function () {
            _.bindAll(this);
            Backbone.Events.on("refresh_tree", this.resetCollection);
        },

        render:function(){
            this.$el.html(this.template({i18n:i18n}));

            this.bindDomElements();

            this.partListView = new PartListView({
                el:this.$("#part_table"),
                collection:new PartCollection()
            }).render();

            this.partListView.collection.on("page-count:fetch",this.onPageCountFetched);
            this.partListView.collection.fetchPageCount();

            this.partListView.on("delete-button:display", this.changeDeleteButtonDisplay);
            this.partListView.on("checkout-group:display", this.changeCheckoutGroupDisplay);
            this.partListView.on("checkout-group:update", this.updateCheckoutButtons);
            this.partListView.on("acl-edit-button:display", this.changeACLButtonDisplay);

            return this;
        },

        bindDomElements:function(){
            this.deleteButton = this.$(".delete");
            this.checkoutGroup = this.$(".checkout-group");
            this.checkoutButton = this.$(".checkout");
            this.undoCheckoutButton = this.$(".undocheckout");
            this.aclButton = this.$(".edit-acl");
            this.checkinButton = this.$(".checkin");
            this.currentPageIndicator =this.$(".current-page");
            this.pageControls =this.$(".page-controls");
        },

        newPart:function(e){
            var partCreationView = new PartCreationView();
            this.listenTo(partCreationView, 'part:created', this.fetchPartAndAdd);
            $("body").append(partCreationView.render().el);
            partCreationView.openModal();
        },

        fetchPartAndAdd:function(part){
            var self = this;
            part.set("partKey",part.getNumber()+"-A");
            part.fetch().success(function(){
                self.addPartInList(part);
            });
            this.partListView.collection.fetchPageCount();
        },

        deletePart:function(){
            this.partListView.deleteSelectedParts();
        },

        addPartInList:function(part){
            this.partListView.pushPart(part)
        },

        changeDeleteButtonDisplay:function(state){
            if(state){
                this.deleteButton.show();
            }else{
                this.deleteButton.hide();
            }
        },

        changeACLButtonDisplay:function(state){
            if(state){
                this.aclButton.show();
            }else{
                this.aclButton.hide();
            }
        },

        changeCheckoutGroupDisplay:function(state){
            if(state){
                this.checkoutGroup.show();
            }else{
                this.checkoutGroup.hide();
            }
        },

        updateCheckoutButtons: function(values) {
            this.checkoutButton.prop('disabled', !values.canCheckout);
            this.undoCheckoutButton.prop('disabled', !values.canUndoAndCheckin);
            this.checkinButton.prop('disabled', !values.canUndoAndCheckin);
        },

        checkin:function(){
            var self = this;
            var selectedPart = this.partListView.getSelectedPart();

            if(_.isNull(selectedPart.getLastIteration().attributes.iterationNote)) {

                var promptView = new PromptView();
                promptView.setPromptOptions(i18n.ITERATION_NOTE, i18n.ITERATION_NOTE_PROMPT_LABEL, i18n.ITERATION_NOTE_PROMPT_OK, i18n.ITERATION_NOTE_PROMPT_CANCEL);
                $("body").append(promptView.render().el);
                promptView.openModal();

                self.listenTo(promptView, 'prompt-ok', function(args) {
                    var iterationNote = args[0];
                    if(_.isEqual(iterationNote, "")) {
                        iterationNote = null;
                    }
                    selectedPart.getLastIteration().save({
                        iterationNote: iterationNote
                    });
                    selectedPart.checkin();
                });

                self.listenTo(promptView, 'prompt-cancel', function() {
                    selectedPart.checkin();
                });
            } else {
                selectedPart.checkin();
            }
        },
        checkout:function(){
            this.partListView.getSelectedPart().checkout();
        },
        undocheckout:function(){
            this.partListView.getSelectedPart().undocheckout();
        },

        updateACL:function(){

            var that = this;

            var selectedPart = that.partListView.getSelectedPart();

            var aclEditView = new ACLEditView({
                editMode:true,
                acl:selectedPart.get("acl")
            });

            aclEditView.setTitle(selectedPart.getPartKey());

            $("body").append(aclEditView.render().el);

            aclEditView.openModal();

            aclEditView.on("acl:update",function(){

                var acl = aclEditView.toList() ;

                selectedPart.updateACL({
                    acl:acl,
                    success:function(){
                        selectedPart.set("acl",acl);
                        aclEditView.closeModal();
                    },
                    error:function(){
                        alert("Error on update acl")
                    }
                });

            });

        },

        resetCollection:function(){
            this.partListView.collection.fetch({reset:true});
        },

        onPageCountFetched:function(){
            this.updatePageIndicator();
            if(this.partListView.collection.hasSeveralPages()){
                this.pageControls.show();
            }else{
                this.pageControls.hide();
            }
        },

        goToPage:function(){
            var requestedPage = prompt(i18n.GO_TO_PAGE,"1");
            if( requestedPage - 1 >= 0 && requestedPage <= this.partListView.collection.getPageCount()){
                this.partListView.collection.setCurrentPage(requestedPage-1).fetch({reset:true});
                this.updatePageIndicator();
                this.partListView.onNoPartSelected();
            }
        },

        toFirstPage:function(){
            this.partListView.collection.setFirstPage().fetch({reset:true});
            this.updatePageIndicator();
            this.partListView.onNoPartSelected();
        },

        toLastPage:function(){
            this.partListView.collection.setLastPage().fetch({reset:true});
            this.updatePageIndicator();
            this.partListView.onNoPartSelected();
        },

        toNextPage:function(){
            this.partListView.collection.setNextPage().fetch({reset:true});
            this.updatePageIndicator();
            this.partListView.onNoPartSelected();
        },

        toPreviousPage:function(){
            this.partListView.collection.setPreviousPage().fetch({reset:true});
            this.updatePageIndicator();
            this.partListView.onNoPartSelected();
        },

        updatePageIndicator:function(){
            this.currentPageIndicator.text(this.partListView.collection.getCurrentPage() + " / " + this.partListView.collection.getPageCount());
        }



    });

    return PartContentView;

});
