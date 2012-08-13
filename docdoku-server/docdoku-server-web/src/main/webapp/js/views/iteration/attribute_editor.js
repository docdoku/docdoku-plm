define([
    "i18n",
    "models/attribute",
    "text!templates/attributes/attribute_item.html"
], function (i18n, Attribute, attributeTemplate) {

    var AttributeEditor = function () {
    };

    AttributeEditor.prototype = {


        tagName:'div',

        initialize:function () {
            this.attribute = new Attribute({
                type:"TEXT",
                name:"",
                value:""
            });
        },

        setWidget:function (widget) {
            this.widget = widget;
            this.customizeWidget(widget);
        },

        customizeWidget:function (widget) {
            var self = this;
            widget.on("list:addItem", function (listElement) {
                var newAttribute = new Attribute({
                    type:Attribute.types.TEXT,
                    name:"",
                    value:""
                });
                widget.trigger("list:added", newAttribute);
            });


            widget.on("list:selected", function (item, index, element) {
                console.log ("index : "+index);
                var component = widget.components[index];
                component.options.isSelected = true;
                component.render();
            })
            widget.on("list:unselected", function (item, index, element) {
                console.log ("index : "+index);
                var component = widget.components[index];
                component.options.isSelected = false;
                component.render();
            })

        },

        getComponent:function (widget, item, isSelected, row) {

            var attributeView = new AttributeView({
                widget:widget,
                model:item,
                isSelected:isSelected,
                row:row
            });
            attributeView.editor = this;

            return attributeView;
        }
    }

    var AttributeView = Backbone.View.extend({

        events:{
            "change .type":"typeChanged",
            "change .attribute-name":"updateName",
         //   "keypress .attribute-name":"updateName",
            "change .attribute-value":"updateValue",
            "keypress .attribute-value":"updateValue"
        },

        initialize : function(){
          this.model.on("error", function(item, message){
                console.error ("item "+item+" not valid");
          });
        },

        render:function () {

            var data = this.dataMapper();
            var html = Mustache.render(attributeTemplate, data);
            this.$el.html(html);

        },

        dataMapper:function () {
            var attribute = this.model;
            var widget = this.options.widget;
            var isSelected = this.options.isSelected;
            var type = attribute.get("type");

            var displayedValue = attribute.get("value");

            if (type == Attribute.types.DATE){
                displayedValue = parseInt(displayedValue);
                displayedValue = $.datepicker.formatDate(
                    i18n["_DATE_PICKER_DATE_FORMAT"],
                    new Date(displayedValue)
                );
            }

            var result =  {
                cid:attribute.cid,
                id:attribute.id,
                type:type,
                typeText : i18n[type],
                name:attribute.get("name"),
                value:displayedValue,
                _:i18n,
                isText:type == Attribute.types.TEXT,
                isBoolean:type == Attribute.types.BOOLEAN,
                isNumber:type == Attribute.types.NUMBER,
                isDate:type == Attribute.types.DATE,
                isUrl:type == Attribute.types.URL,
                isSelected:isSelected
            }
                return result;
        },

        typeChanged:function (evt) {
            var type = $(evt.target).val();
            console.log("changed model type to" + type);
            var defaultValue = type == Attribute.types.BOOLEAN ? false : "";
            this.model.set({
                type:type,
                value : defaultValue
            }, {silent:true});

            this.render()
            this.editor.trigger("attributeChanged",  this.model);
        },
        updateName:function () {
            var attributeName =this.$el.find("input.attribute-name").val();
            this.model.set({
                name:attributeName
            }, {silent:true});
            this.editor.trigger("attributeChanged",  this.model);
        },
        updateValue:function (evt) {

            var attributeValue = this.$el.find("input.attribute-value").val();

            if (this.model.getType() == Attribute.types.NUMBER && kumo.isNotEmpty(attributeValue)){
                attributeValue = kumo.replaceAll(attributeValue, "\\,", ".");
                this.$el.find("input.attribute-value").val(attributeValue);
                attributeValue = parseFloat(attributeValue);
            }

            if (this.model.getType() == Attribute.types.BOOLEAN ){
                attributeValue =  this.$el.find("input.attribute-value").is(":checked");
            }

            if (this.model.getType() == Attribute.types.DATE){
              /*  attributeValue = $.datepicker.formatDate(
                    i18n["_DATE_PICKER_DATE_FORMAT"],
                    new Date(attributeValue)
                );*/

                attributeValue = $.datepicker.parseDate(
                    i18n["_DATE_PICKER_DATE_FORMAT"],
                    attributeValue
                ).getTime();

                kumo.debug("date : "+attributeValue);
            }

            this.model.set({
                value:attributeValue
            }, {silent:true});

            if (this.model.getType() == Attribute.types.BOOLEAN ){
                this.render()
            }

            this.editor.trigger("attributeChanged",  this.model);

        }
    });

    return AttributeEditor;
});

