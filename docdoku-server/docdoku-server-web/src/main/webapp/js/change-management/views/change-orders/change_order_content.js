define([
    "collections/change_order_collection",
    "text!templates/change-orders/change_order_content.html",
    "i18n!localization/nls/change-management-strings",
    "views/change-orders/change_order_list",
    "text!common-objects/templates/buttons/delete_button.html",
    "text!common-objects/templates/buttons/tags_button.html",
    "text!common-objects/templates/buttons/ACL_button.html"
], function (
    ChangeOrderCollection,
    template,
    i18n,
    ChangeOrderListView,
    delete_button,
    tags_button,
    ACL_button
    ) {
    var ChangeOrderContentView = Backbone.View.extend({

        template: Mustache.compile(template),
        events:{
            "click button.new-order":"newOrder",
            "click button.delete":"deleteOrder",
            "click button.edit-acl":"actionEditAcl",
            "click button.tags":"actionTags"
        },

        partials:{
            delete_button: delete_button,
            tags_button: tags_button,
            ACL_button: ACL_button
        },

        initialize: function () {
            _.bindAll(this);
            this.collection = new ChangeOrderCollection();
        },

        render:function(){
            this.$el.html(this.template({i18n:i18n}, this.partials));

            this.bindDomElements();

            this.listView = new ChangeOrderListView({
                el:this.$("#order_table"),
                collection:this.collection
            }).render();

            this.listView.on("delete-button:display", this.changeDeleteButtonDisplay);
            this.listView.on("acl-button:display", this.changeAclButtonDisplay);
            this.tagsButton.show();

            this.$el.on("remove",this.listView.remove);
            return this;
        },

        bindDomElements:function(){
            this.deleteButton = this.$(".delete");
            this.aclButton = this.$(".edit-acl");
            this.tagsButton = this.$(".tags");
        },

        newOrder:function(){
            var self = this;
            require(["views/change-orders/change_order_creation"],function(ChangeOrderCreationView){
                var orderCreationView = new ChangeOrderCreationView({
                    collection:self.collection
                });
                $("body").append(orderCreationView.render().el);
                orderCreationView.openModal();
            });
        },

        deleteOrder:function(){
            this.listView.deleteSelectedOrders();
        },

        actionTags:function(){
            var changeOrdersChecked = new Backbone.Collection();


            this.listView.eachChecked(function(view) {
                changeOrdersChecked.push(view.model);
            });

            require(["common-objects/views/tags/tags_management"],function(TagsManagementView){
                var tagsManagementView = new TagsManagementView({
                    collection: changeOrdersChecked
                });
                $("body").append(tagsManagementView.el);
                tagsManagementView.show();
            });

            return false;
        },

        actionEditAcl: function(){
            var modelChecked = this.listView.getChecked();

            if(modelChecked){
                var self = this;
                modelChecked.fetch();
                require(["common-objects/views/security/acl_edit"],function(ACLEditView){
                    var aclEditView = new ACLEditView({
                        editMode:true,
                        acl:modelChecked.getACL()
                    });

                    aclEditView.setTitle(modelChecked.getName());
                    $("body").append(aclEditView.render().el);

                    aclEditView.openModal();
                    aclEditView.on("acl:update",function(){
                        var acl = aclEditView.toList();
                        modelChecked.updateACL({
                            acl: acl||{userEntries:{},groupEntries:{}},
                            success:function(){
                                modelChecked.set("acl",acl);
                                aclEditView.closeModal();
                                self.listView.redraw();
                            },
                            error:function(){
                                alert("Error on update acl");
                            }
                        });

                    });
                });
            }
        },

        changeDeleteButtonDisplay:function(state){
            if(state){
                this.deleteButton.show();
            }else{
                this.deleteButton.hide();
            }
        },

        changeAclButtonDisplay:function(state){
            if(state){
                this.aclButton.show();
            }else{
                this.aclButton.hide();
            }
        }
    });

    return ChangeOrderContentView;
});