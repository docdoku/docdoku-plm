define(function() {

    var expandedViews = [];
    var ComponentViews = {};

    ComponentViews.Components = Backbone.View.extend({

        tagName:'ul',

        initialize: function() {
            this.options.parentView.append(this.el);
            this.componentViews = [];
            this.expandedViews = [];
            if (this.collection.isEmpty()) {
                this.listenTo(this.collection, 'reset', this.addAllComponentsView)
                    .listenTo(this.collection, 'add', this.addComponentView);
                this.collection.fetch();
            } else {
                this.addAllComponentsView();
            }
        },

        addAllComponentsView: function() {
            this.collection.each(this.addComponentView, this);
        },

        addComponentView: function(component) {

            var isLast = component == this.collection.last();

            var optionsForComponentView = {
                model: component,
                isLast: isLast,
                checkedAtInit: this.options.parentChecked,
                resultPathCollection: this.options.resultPathCollection
            };

            var componentView = component.isAssembly() ? new ComponentViews.Assembly(optionsForComponentView) : new ComponentViews.Leaf(optionsForComponentView);

            this.$el.append(componentView.render().el);

            // expand the assembly if it was expanded before redraw
            if(component.isAssembly() && _.contains(expandedViews,component.attributes.number)){
                componentView.onToggleExpand();
            }

            this.componentViews.push(componentView);
        },

        fetchAll:function(){
            var that = this ;
            this.$el.empty();
            this.collection.fetch();
        }

    });

    ComponentViews.Leaf = Backbone.View.extend({

        tagName:'li',

        template: _.template("<input type='checkbox' <%if (checkedAtInit) {%>checked='checked'<%}%>><a href='#'><label class='checkbox'><%= number %> (<%= amount %>)</label></a><i class='icon-file'></i>"),

        events: {
            "click a": "onComponentSelected",
            "change input:first": "onChangeCheckbox",
            "click .icon-file:first": "onEditPart"
        },

        initialize: function() {
            _.bindAll(this, ["onChangeCheckbox"]);
            this.listenTo(this.options.resultPathCollection, 'reset', this.onAllResultPathAdded);
        },

        onAllResultPathAdded: function() {
            if (this.options.resultPathCollection.contains(this.model.attributes.partUsageLinkId)) {
                this.$el.addClass("resultPath");
            } else {
                this.$el.removeClass("resultPath");
            }
        },

        onChangeCheckbox: function(event) {
            if (event.target.checked){
                this.model.putOnScene();
            }
            else{
                this.model.removeFromScene();
            }
        },

        render: function() {

            this.$el.html(this.template({number: this.model.attributes.number, amount: this.model.getAmount(), checkedAtInit: this.options.checkedAtInit}));

            if (this.options.isLast) {
                this.$el.addClass('last');
            }

            this.onAllResultPathAdded();

            return this;
        },

        onComponentSelected: function(e) {
            e.stopPropagation();
            this.$("a").trigger("component_selected", [this.model]);
        },

        onEditPart:function(){
            var self = this;
            require(['models/part','views/part_modal_view'], function(Part,PartModalView) {
                var model = new Part({partKey:self.model.getNumber() + "-" + self.model.getVersion()});
                model.fetch().success(function(){
                    new PartModalView({
                        model: model
                    }).show();
                });

            });
        }

    });

    ComponentViews.Assembly = Backbone.View.extend({

        tagName:'li',

        className: 'expandable',

        template: _.template("<div class=\"hitarea expandable-hitarea\"></div><input type='checkbox' <%if (checkedAtInit) {%>checked='checked'<%}%>><a href='#'><label class='checkbox isNode'><%= number %> (<%= amount %>)</label></a><i class='icon-file'></i>"),

        events: {
            "click a:first": "onComponentSelected",
            "click .icon-file:first": "onEditPart",
            "change input:first": "onChangeCheckbox",
            "click .hitarea:first": "onToggleExpand"
        },

        initialize: function() {
            this.isExpanded = false;
            _.bindAll(this, ["onChangeCheckbox"]);
            this.listenTo(this.options.resultPathCollection, 'reset', this.onAllResultPathAdded);
        },

        onAllResultPathAdded: function() {
            if (this.options.resultPathCollection.contains(this.model.attributes.partUsageLinkId)) {
                this.$el.addClass("resultPath");
            } else {
                this.$el.removeClass("resultPath");
            }
        },

        onChangeCheckbox: function(event) {
            if (event.target.checked){
                this.model.putOnScene();
            }
            else{
                this.model.removeFromScene();
            }
        },

        render: function() {

            this.$el.html(this.template({number: this.model.attributes.number, amount: this.model.getAmount(), checkedAtInit: this.options.checkedAtInit}));

            this.input = this.$(">input");

            if (this.options.isLast) {
                this.$el.addClass('lastExpandable')
                    .children('.hitarea')
                    .addClass('lastExpandable-hitarea');
            }

            this.onAllResultPathAdded();


            if(_.contains(this.expandedViews,this.model.attributes.number)){
                this.onToggleExpand();
            }

            return this;
        },

        onToggleExpand: function() {
            this.toggleExpand();
            if (!this.hasChildrenNodes()) {
                new ComponentViews.Components({
                    collection: this.model.children,
                    parentView: this.$el,
                    parentChecked: this.isChecked(),
                    resultPathCollection: this.options.resultPathCollection
                });
            }
        },

        toggleExpand: function() {
            this.$el.toggleClass('expandable collapsable')
                .children('.hitarea')
                .toggleClass('expandable-hitarea collapsable-hitarea');

            if (this.options.isLast) {
                this.$el.toggleClass('lastExpandable lastCollapsable')
                    .children('.hitarea')
                    .toggleClass('lastExpandable-hitarea lastCollapsable-hitarea');
            }

            this.isExpanded = !this.isExpanded;

            var childrenNode = this.$(">ul");

            if(this.isExpanded){
                childrenNode.show();
                expandedViews.push(this.model.attributes.number);
            }else{
                childrenNode.hide();
                expandedViews = _(expandedViews).without(this.model.attributes.number);
            }
        },

        hasChildrenNodes: function() {
            var childrenNode = this.$(">ul");
            return childrenNode.length > 0;
        },

        onComponentSelected: function(e) {
            e.stopPropagation();
            this.$("a").trigger("component_selected", [this.model]);
        },

        isChecked: function() {
            return this.input.prop('checked');
        },

        onEditPart:function(){
            var self = this;
            require(['models/part','views/part_modal_view'], function(Part,PartModalView) {
                var model = new Part({partKey:self.model.getNumber() + "-" + self.model.getVersion()});
                model.fetch().success(function(){
                    new PartModalView({
                        model: model
                    }).show();
                });

            });
        }

    });

    return ComponentViews;
});
