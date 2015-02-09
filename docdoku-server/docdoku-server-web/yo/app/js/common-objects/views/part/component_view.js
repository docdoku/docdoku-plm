/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/part/component_view.html',
    'common-objects/views/part/substitute_part_view',
    'common-objects/views/part/cad_instance_view'
], function (Backbone, Mustache, template, SubstitutePartView, CadInstanceView) {
    'use strict';
    var ComponentView = Backbone.View.extend({

        events: {
            'click a.remove': 'onRemove',
            'change input[name=amount]': 'changeAmount',
            'change input[name=comment]': 'changeComment',
            'change input[name=number]': 'changeNumber',
            'change input[name=name]': 'changeName',
            'input input[name=newUnit]': 'changeMeasureUnit',
            'click datalist[name=unitMeasure]': 'changeMeasureUnit',
            'click .add-cadInstance': 'addCadInstance',
            'click .collapse-cadInstance': 'collapseTransformations',
             'click .add-substitute-part': 'displaySubstituteParts',
             'click .create-substitute-part': 'addSubstitutePart',
             'click .hide-substitute-part': 'hideSubstitutePartsView'
        },


        initialize: function () {
        },

        render: function () {
            this.substitutePartViews = [];
            this.$el.html(Mustache.render(template, {
                model: this.model.attributes,
                i18n: App.config.i18n,
                editMode: this.options.editMode
            }));
            this.bindDomElements();
            this.initCadInstanceViews();
            this.initSubstitutePartView();
            this.initUnit();
            return this;
        },

        bindDomElements: function () {
            this.$cadInstances = this.$('.cadInstances');
            this.$amount = this.$('input[name=amount]');
            this.$comment = this.$('input[name=comment]');
            this.$unitText = this.$('input[name=newUnit]');
            this.$defaultUnity = this.$unitText.attr('default-unity');
            this.$collapseButton = this.$('.collapse-cadInstance');
            this.$substitutePartButton = this.$('.substitute-part');
            this.$subtituteParts = this.$('.substitutes');
        },

        initCadInstanceViews: function () {
            var self = this;
            _(this.model.get('cadInstances')).each(function (instance) {
                self.addCadInstanceView(instance);
                self.$cadInstances.hide();
            });
        },

        initSubstitutePartView: function (){

            var self = this;
            _(this.model.get('substitutes')).each(function (instance) {
                self.addSubstitutePartsView(instance);
            });
//            self.$('.substitute-parts').hide();
        },
        initUnit: function () {
            var unit = this.model.get('unit');
            this.$unitText.val(unit ? unit : this.$unitText.attr('default-unity'));
            this.disableEnableAmount(unit);
        },

        addCadInstanceView: function (instance) {
            var self = this;
            var instanceView = new CadInstanceView({editMode:this.options.editMode});
            instanceView.setInstance(instance).render();
            self.$cadInstances.append(instanceView.$el);
            instanceView.on('instance:remove', function () {
                self.onRemoveCadInstance(instance);
            });
        },
        addSubstitutePartsView: function (instance) {

            var self = this;
            self.$substitutePartButton.hide();
            var substitutePartView = new SubstitutePartView({model:  this.model, editMode: this.options.editMode, removeHandler: function () {
                self.collection.remove( this.model);
            }});
            substitutePartView.setInstance(instance).render();
            this.substitutePartViews.push(substitutePartView);
            this.$subtituteParts.append(substitutePartView.$el);

        },
        /*addSubtitutePart: function(){

            if (!this.$editSubtituteMode){
                $("#iteration-components .well").hide();
                this.$(".well").show();
                this.$(".subtitute-parts").show();
                $("#createPart").hide();
                $("#existingParts").hide();
                this.$collapseButton.hide();
                this.$(".add-cadInstance").hide();
                this.$editSubtituteMode = true;
            }
            else{
                $("#createPart").show();
                $("#existingParts").show();
                this.$(".subtitute-parts").hide();
                $("#iteration-components .well").show();
                this.$collapseButton.show();
                this.$(".add-cadInstance").show();
                this.$editSubtituteMode = false;
            }
        },*/

        onRemove: function () {
            if (this.options.removeHandler && this.options.editMode) {
                this.options.removeHandler();
            }
        },

        onRemoveCadInstance: function (instance) {
            this.model.set('cadInstances', _(this.model.get('cadInstances')).without(instance));
            this.$amount.val(parseInt(this.$amount.val(), 10) - 1);
            this.model.set('amount', this.$amount.val());
            this.model.get('component').amount = this.$amount.val();
        },

        addCadInstance: function () {
            var instance = {tx: 0, ty: 0, tz: 0, rx: 0, ry: 0, rz: 0};
            this.model.get('cadInstances').push(instance);
            this.addCadInstanceView(instance);
            this.$amount.val(parseInt(this.$amount.val(), 10) + 1);
            this.model.set('amount', this.$amount.val());
        },

        addSubstitutePart: function(){
            var substitutePart = {
                number:'',
                name:'',
                amount: this.model.get('amount'),
                substitute:this.model.get('component'),
                cadInstances: [
                    {tx: 0, ty: 0, tz: 0, rx: 0, ry: 0, rz: 0}
                ],
                unit: this.model.get('unit'),
                comment: '',
                referenceDescription : ''
            };

            this.model.get('substitutes').push(substitutePart);
            this.addSubstitutePartsView(substitutePart);
        },
        collapseTransformations: function () {
            var isVisible = this.$cadInstances.is(':visible');
            this.$cadInstances.toggle(!isVisible);
            this.$collapseButton.toggleClass('fa-angle-double-down', isVisible);
            this.$collapseButton.toggleClass('fa-angle-double-up', !isVisible);
        },
        changeAmount: function (e) {
            this.model.set('amount', e.target.value);
            this.model.get('component').amount = e.target.value;
        },
        changeComment: function (e) {
            this.model.set('comment', e.target.value);
        },
        changeNumber: function (e) {
            this.model.get('component').number = e.target.value;
        },
        changeName: function (e) {
            this.model.get('component').name = e.target.value;
        },
        changeMeasureUnit: function(e){
            this.model.set('unit',e.target.value);
            this.$unitText.val(e.target.value);
            this.disableEnableAmount(e.target.value);
        },
        checkIntegrity: function (unit) {

            if (unit == "null" || unit == "" || unit == undefined || unit == this.$defaultUnity ) {
                if ( parseInt(this.$amount.val(),10) > this.$('.cadInstance').length) {
                    var totalUnitToAdd =  parseInt(this.$amount.val(),10) - this.$('.cadInstance').length;
                    for(var i=0;i<totalUnitToAdd;i++){
                        var instance = {tx: 0, ty: 0, tz: 0, rx: 0, ry: 0, rz: 0};
                        this.model.get('cadInstances').push(instance);
                        this.addCadInstanceView(instance);
                    }
                }
                if(parseInt(this.$amount.val(),10) < this.$('.cadInstance').length) {
                    var totalToDelete= this.$('.cadInstance').length-parseInt(this.$amount.val(),10);
                    this.$(".cadInstance").slice(-totalToDelete).remove();
                }
            }else  {
                if( this.$('.cadInstance').length > 1){
                    this.$(".cadInstance:not(:first)").remove();
                    var self= this;
                    _.each(self.model.get('cadInstances'),function(){
                        self.model.get('cadInstances').pop();
                    });

                }
            }
        },
        disableEnableAmount: function(unit){

            if (unit == "null" || unit == "" || unit == undefined || unit == this.$defaultUnity)
            {
                this.$amount.val(parseInt(this.$amount.val(),10)== 0 ? 1:parseInt(this.$amount.val(),10));
                this.$amount.attr('disabled','disabled');
                this.$('.add-cadInstance').show();
                this.$unitText.val(this.$unitText.attr('default-unity'));
            }
            else{
                this.$amount.removeAttr('disabled');
                this.$('.add-cadInstance').hide();
            }
            this.checkIntegrity(unit);

        }


    });

    return ComponentView;
});
