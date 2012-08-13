define([
    "i18n"
], function (i18n) {
    var EditableListView = Backbone.View.extend({

        debug:true,
        className:'editable-list',
        // id:this.cid,




        tagName:'ul',

        initialize:function () {
            var self = this;
            //this.$el.addClass('editable-list');
            //Check validity

            if (kumo.devMode) {
                if (kumo.isEmpty(this.options.listName)) {
                    console.log("no listName set ; please set it for easier debug")
                }

                if (kumo.isNotEmpty(this.options.itemPartial)) {
                    if (kumo.isEmpty(this.options.dataMapper)) {
                        console.error("no dataMapper set to the List " + this.listName);
                    }
                } else {
                    kumo.assertNotEmpty(this.options.editor, "a List must have either a partial, either a Component Editor");
                }
            }
            if (kumo.enableAssert) {
                kumo.assert(_.isArray(this.getModels()), "EditableListView.model is not an array. There will be bugs");


                if (kumo.isNotEmpty(this.options.editor)) {
                    kumo.assertNotEmpty(this.options.editor.getComponent,
                        "an editor must have a getComponent(widget, item, isSelected, row) method to render the item"
                    )
                }

                //checking each object is a Model with an cid
                _.each(this.getModels(), function (item) {
                    kumo.assert(kumo.isNotEmpty(item.cid), "items in the list model must have a cid");
                });
            }


            //initialize :

            //events
            this.on("state:working", this.onWorking);
            this.on("state:idle", this.onIdle);
            this.on("state:cancel", this.onCancel);
            this.on("list:added", this.onItemAdded);

            /**
             * Also declaring for other objects :
             * list:addItem -> the ul element
             * list:selected -> selectedObject, li element
             * list:unselected -> unselectedObject, li element
             */


                //state lists
            this.components = []; // Warning : Backbone Collections takes only Models object.
            this.selection = new Backbone.Collection();
            this.newItems = new Backbone.Collection();

        },

        //DOM Events
        events:{
            "click .editable-list-cancel-editor":"bindCancelButton",
            "change input.item-selection":"bindItemSelected",
            "click .editable-list-adder":"bindAddItem"
        },


        //states
        onWorking:function () {
          /*  try {
                this.getAddButton().attr('disabled', 'disabled');
            } catch (e) {
                //no addButton
            }


            try {
                this.getCancelButton().show();
            } catch (e) {
                //no cancelButton
            }*/


        },

        onIdle:function () {
          /*  try {
                this.getAddButton().removeAttr('disabled');
            } catch (e) {
                //no addButton
            }

            try {
                this.getCancelButton().hide();
            } catch (e) {
                //no cancelButton
            }*/


        },

        onCancel:function () {
            this.onIdle();
        },

        getModels:function () {
            return this.model.models;
        },

        bindAddItem:function () {
            this.trigger("state:working");
            this.trigger("list:addItem", this.getCreationEditorPlace());
        },

        bindCancelButton:function () {
            //this.getCreationEditorPlace().empty();
            this.trigger("state:cancel");
        },

        bindItemSelected:function (evt) {
            var checkbox = $(evt.target);
            var cid = checkbox.val();
            var selectedObject = this.model.getByCid(cid);
            kumo.assertNotEmpty(selectedObject, "Can't find selectedObject");
            var index = this.model.indexOf(selectedObject);

            if (checkbox.is(":checked")) {
                this.trigger("list:selected", selectedObject, index, checkbox.parents("li"));
                kumo.assert(!_.include(this.selection, selectedObject), "The selection already contains the selectedObject :" + selectedObject);
                this.selection.add(selectedObject);
            } else {
                this.trigger("list:unselected", selectedObject, index, checkbox.parents("li"));
                kumo.assert(!_.include(this.selection, selectedObject), "The selection does not the selectedObject " + selectedObject);
                this.selection.remove(selectedObject);
            }

        },

        render:function () {

            var itemPartial, itemsData;
            if (this.options.itemPartial) {
                itemPartial = this.options.itemPartial
                itemsData = this.model.map(this.options.dataMapper);//Extract data from the item collection
            } else if (this.options.editor) {
                itemPartial = "<div class='item-component' data-cid='{{cid}}'></div>";
                itemsData = this.model.map(function (item) {
                    return {
                        cid:item.cid
                    }
                }); // returns only cid

            } else {
                console.error("A list must render either item partials, or item components");
            }

            var fullTemplate = this.fullTemplate();

            var data = {
                listId:this.cid,
                items:itemsData,
                editable:this.options.editable,
                i18n:i18n
            };

            var html = Mustache.render(fullTemplate,
                data,
                {itemPartial:itemPartial}
            );
            this.$el.html(html);

            if (this.options.editor) {
                this.renderComponents();
            }

            this.getCancelButton().hide();
            this.trigger("component:rendered");
            this.trigger("state:idle");

            return this;
        },

        renderComponents:function () {
            var widget = this;
            var editor = this.options.editor;

            var row = 0;
            this.components = [];
            this.model.each(function (item) {
                console.log("row : " + row);
                var isSelected = widget.selection.include(item);
                var component = editor.getComponent(widget, item, isSelected, row);
                widget.components.push(component);
                var element = widget.$el.find("div.item-component")[row];
                component.render();
                $(element).append(component.$el);
                row++;
            });
        },


        fullTemplate:function () {
            return this.listTemplate() + this.creationEditorTemplate() + this.controlBarTemplate();
        },

        listTemplate:function () {

            var list =
                "{{#items}}\n" +
                    "<li id='item-{{cid}}' class='list-item editable-list-item'>\n" +
                    "{{#editable}}<input class='item-selection' type='checkbox' value='{{cid}}' />{{/editable}}\n" + //delete Button
                    "{{>itemPartial}}" + //custom display of the object
                    "</li>\n" +
                    "{{/items}}\n";


            return list;
        },


        creationEditorTemplate:function () {
            return "<div id='editable-list-editor-{{listId}}' class='editable-list-editor'></div>"
        },


        getCreationEditorPlace:function () {
            var elt = this.$el.find("#editable-list-editor-" + this.cid);
            kumo.assertNotEmpty(elt, "can't find editor-place element");
            return elt;
        },

        controlBarTemplate:function () {
            var controls = "{{#editable}}<div id='editable-list-controls-{{listId}}'>" +
                "<button id='editable-list-add-item-{{listId}}' class='btn editable-list-adder'>{{i18n.APPEND}}</button>" +
                "<button id='editable-list-cancel-editor-{{listId}}' class='btn cancel editable-list-cancel-editor'>{{i18n.CANCEL}}</button>" +
                "</div>{{/editable}}\n";
            return controls;
        },


        onItemAdded:function (item) {

            this.model.add(item);
            this.getNewItems().add(item); // later list is disabled
            this.render();
            this.trigger("state:idle");

        },


        getNewItems:function () {
            //kumo.assert(this.options.editable, "Can't get new items if not editable");
            return this.newItems;
        },

        getUnselectedItems:function () {
            var selection = this.selection;
            var result = new Backbone.Collection();
            this.model.each(function (item) {
                if (!selection.include(item)) {
                    result.push(item);
                }
            });
            return result;
        },


        getControlsElement:function () {
            var controlsElement = $("#editable-list-controls-" + this.cid);
            kumo.assertNotEmpty(controlsElement, "can't find control element for list " + this.listName);
            return controlsElement;
        },

        getListElement:function () {
            var listElement = this.$el.find("ul:first-child");
            kumo.assertNotEmpty(listElement, "The listElement is not yet in the DOM");
            return listElement;
        },

        setControls:function (html) {
            this.getControlsElement().html(html);
        },

        getAddButton:function () {
            var button = this.$el.find(".editable-list-adder");
            kumo.assertNotEmpty(button, "Can't find addButton");
            return button;
        },

        getCancelButton:function () {
            var button = this.$el.find("button.editable-list-cancel-editor");
            kumo.assertNotEmpty(button, "Can't find cancel Button");
            return button;
        }

    });
    return EditableListView;
});
