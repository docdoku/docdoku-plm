'use strict';
define([
    'common-objects/collections/baselines',
    'common-objects/collections/configuration_items',
    'text!templates/baseline/baselines_content.html',
    'i18n!localization/nls/baseline-strings',
    'views/baseline/baselines_list',
    'views/baseline/baseline_duplicate_view',
    'text!common-objects/templates/buttons/delete_button.html',
    'text!common-objects/templates/buttons/duplicate_button.html'
], function (
    BaselinesCollection,
    ConfigurationItemCollection,
    template,
    i18n,
    BaselinesListView,
    BaselineDuplicateView,
    deleteButton,
    duplicateButton
    ) {
    var BaselinesContentView = Backbone.View.extend({

        template: Mustache.compile(template),

        partials:{
            deleteButton: deleteButton,
            duplicateButton: duplicateButton
        },

        events:{
            'click button.delete':'deleteBaseline',
            'click button.duplicate': 'duplicateBaseline'
        },

        initialize: function () {
            _.bindAll(this);
        },

        render:function(){
            this.$el.html(this.template({i18n:i18n},this.partials));

            this.bindDomElements();
            new ConfigurationItemCollection().fetch({success: this.fillProductList});
            var self = this ;
            this.$inputProductId.change(function(){self.createBaselineView();});
            this.createBaselineView();
            return this;
        },

        bindDomElements:function(){
            this.deleteButton = this.$('.delete');
            this.duplicateButton = this.$('.duplicate');
            this.$inputProductId = this.$('#inputProductId');
        },

        fillProductList: function(list){
            var self = this ;
            if(list){
                list.each(function(product){
                    self.$inputProductId.append('<option value="'+product.getId()+'" >'+product.getId()+'</option>');
                });
                this.$inputProductId.combobox({bsVersion: '2'});
            }
        },

        createBaselineView: function(){
            if(this.listView){
                this.listView.remove();
                this.changeDeleteButtonDisplay(false);
                this.changeDuplicateButtonDisplay(false);
            }
            if(this.$inputProductId.val()){
                this.listView = new BaselinesListView({
                    collection:new BaselinesCollection({},{type:'product',productId:this.$inputProductId.val()})
                }).render();
            }else{
                this.listView = new BaselinesListView({
                    collection:new BaselinesCollection({},{type:'product'})
                }).render();
            }
            this.$el.append(this.listView.el);
            this.listView.on('delete-button:display', this.changeDeleteButtonDisplay);
            this.listView.on('duplicate-button:display', this.changeDuplicateButtonDisplay);
        },

        deleteBaseline:function(){
            this.listView.deleteSelectedBaselines();
        },

        duplicateBaseline:function(){
            var baselineDuplicateView = new BaselineDuplicateView({model: this.listView.getSelectedBaseline()});
            $('body').append(baselineDuplicateView.render().el);
            baselineDuplicateView.openModal();
        },

        changeDeleteButtonDisplay:function(state){
            if(state){
                this.deleteButton.show();
            }else{
                this.deleteButton.hide();
            }
        },

        changeDuplicateButtonDisplay:function(state){
            if(state){
                this.duplicateButton.show();
            }else{
                this.duplicateButton.hide();
            }
        }
    });

    return BaselinesContentView;
});
