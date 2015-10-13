/*global _,define,App*/
define([
    'backbone',
    'common-objects/models/part',
    'common-objects/views/part/part_modal_view'
], function (Backbone, Part, PartModalView) {
	'use strict';
    var expandedViews = [];
    var ComponentViews = {};

    var nodeTemplate = _.template(
        '<%if(path){%>'+
            '<input id="select-<%= path %>" type="checkbox" class="selectable-part-checkbox">' +
        '<%}%>'+
        '<%if(!isLock && !isForbidden) {%>' +
            '<%if(isNode) {%>' +
                '<div class="hitarea expandable-hitarea"></div>' +
            '<%}%>' +
            '<%if(path){%>'+
                '<input id="load-3D-<%= path %>" type="checkbox" class="load-3D available" <%if (checkedAtInit) {%>checked="checked"<%}%>>' +
                '<label for="load-3D-<%= path %>"><i class="toggle-3D fa"></i></label>' +
            '<%}%>'+
        '<%} else {%>' +
            '<input type="checkbox" class="load-3D" disabled <%if (checkedAtInit) {%>checked="checked"<%}%>>' +
        '<%}%>' +
            '<a><label class="checkbox <%if(isNode) {%>isNode<%}%>">' +
        '<%if(isSubstitute) {%> ' +
            '<i class="fa fa-arrows-h" title="'+App.config.i18n.IS_SUBSTITUTE+'"></i>' +
        '<%}%>' +
        '<%if(hasSubstitutes) {%> ' +
            '<i class="fa fa-random" title="'+App.config.i18n.HAS_SUBSTITUTES+'"></i>' +
        '<%}%>' +
        '<%if(isOptional) {%> ' +
            '<i class="fa fa-question" title="'+App.config.i18n.OPTIONAL+'"></i>' +
        '<%}%>' +
        '<%if(path) {%> ' +
            '<%= name %> < <%= number %>-<%= version %>-<%= iteration %> > (<%= amount %><%if (unit) {%> <%= unit %>  <%}%>)  </label>' +
        '<%}else{%>' +
            '<%= number %>' +
        '<%}%>' +
        '</a>' +
        '<%if(isForbidden) {%> ' +
            '<i class="fa fa-key"></i>' +
        '<%} else if(isCheckoutByAnotherUser && isLastIteration) {%> ' +
            '<i class="fa openModal fa-lock"></i>' +
        '<%} else if(isCheckoutByConnectedUser && isLastIteration) {%> ' +
            '<i class="fa openModal fa-pencil"></i> ' +
        '<%} else if(isReleased){%> ' +
            '<i class="fa openModal fa-check"></i>' +
        '<%} else if(isObsolete){%> ' +
            '<i class="fa openModal fa-frown-o"></i>' +
        '<%} else if(path) {%> ' +
            '<i class="fa openModal fa-eye"></i>' +
        '<%}%>'+
        '<%if(hasUnreadModificationNotifications) {%> ' +
            '<i class="fa fa-exclamation"></i>' +
        '<%}%>' +
        '<%if(hasPathData) {%> ' +
            '<i class="fa fa-asterisk" title="'+App.config.i18n.PRODUCT_INSTANCE_DATA+'"></i>' +
        '<%}%>' +
        '<%if (partUsageLinkReferenceDescription) {%><span class="description"> <%= partUsageLinkReferenceDescription %> </span><%}%>'
    );

    ComponentViews.Components = Backbone.View.extend({

        tagName: 'ul',

        initialize: function () {
            this.options.parentView.append(this.el);
            this.componentViews = [];
            this.expandedViews = [];
            if (this.collection.isEmpty()) {
                this.listenTo(this.collection, 'reset', this.addAllComponentsView)
                    .listenTo(this.collection, 'add', this.addComponentView);
                this.collection.fetch({reset: true});
            } else {
                this.addAllComponentsView();
            }
        },

        addAllComponentsView: function () {
            this.collection.each(this.addComponentView, this);
        },

        addComponentView: function (component) {

            var isLast = component === this.collection.last();

            var optionsForComponentView = {
                model: component,
                isLast: isLast,
                checkedAtInit: this.options.parentChecked || App.partsTreeView.smartPath.indexOf(component.getPath()) !== -1,
                resultPathCollection: this.options.resultPathCollection
            };

            var componentView = component.isAssembly() || App.config.linkType ? new ComponentViews.Assembly(optionsForComponentView) : new ComponentViews.Leaf(optionsForComponentView);

            this.$el.append(componentView.render().el);

            // expand the assembly if it was expanded before redraw && _.contains(expandedViews,component.attributes.number)
            if (component.isAssembly() && _.contains(expandedViews, component.attributes.number)) {
                componentView.onToggleExpand();
            }

            this.componentViews.push(componentView);
        },

        fetchAll: function () {
            this.$el.empty();
            this.collection.fetch({reset: true});
        },

        setChecked: function(status) {
            this.$el.find('.selectable-part-checkbox').prop('checked',status);
        }

    });

    ComponentViews.Leaf = Backbone.View.extend({

        tagName: 'li',

        events: {
            'click a': 'onComponentSelected',
            'change input.load-3D:first': 'onLoad3D',
            'change input.selectable-part-checkbox:first': 'selectPart',
            'click .openModal:first': 'onEditPart',
            'click > .fa-asterisk': 'openPathDataModal'
        },

        initialize: function () {
            _.bindAll(this, ['onLoad3D']);
            this.listenTo(this.options.resultPathCollection, 'reset', this.onAllResultPathAdded);
            this.$el.attr('id', 'path_' + String(this.model.getEncodedPath()));
            this.isForbidden = this.model.isForbidden();
            this.isLock = this.model.isCheckout() && this.model.isLastIteration(this.model.get('iteration')) && !this.model.isCheckoutByConnectedUser();
        },

        onAllResultPathAdded: function () {
            var isInResultPaths = this.options.resultPathCollection.contains(this.model.attributes.partUsageLinkId);
            this.$el.toggleClass('resultPath',isInResultPaths);
        },

        selectPart:function(e){
            this.$el.trigger('checkbox:selected', [e.target.checked,this.model]);
            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        onLoad3D: function (event) {
            if (event.target.checked) {
                App.instancesManager.loadComponent(this.model);
            }
            else {
                App.instancesManager.unLoadComponent(this.model);
            }
        },

        render: function () {

            var data = {
                isNode:false,
                number: this.model.attributes.number,
                name: this.model.attributes.name,
                amount: this.model.getAmount(),
                version: this.model.getVersion(),
                iteration: this.model.getIteration(),
                isLastIteration: this.model.isLastIteration( this.model.getIteration()),
                unit: this.model.getUnit(),
                checkedAtInit: this.options.checkedAtInit,
                isForbidden: this.model.isForbidden(),
                isCheckoutByAnotherUser: this.model.isCheckout() && !this.model.isCheckoutByConnectedUser(),
                isCheckoutByConnectedUser: this.model.isCheckout() && this.model.isCheckoutByConnectedUser(),
                hasUnreadModificationNotifications: this.model.hasUnreadModificationNotifications(),
                isReleased: this.model.isReleased(),
                isObsolete: this.model.isObsolete(),
                isLock: this.isLock,
                partUsageLinkReferenceDescription: this.model.getPartUsageLinkReferenceDescription(),
                isSubstitute: this.model.isSubstitute(),
                isOptional:this.model.isOptional(),
                hasSubstitutes : this.model.hasSubstitutes(),
                hasPathData:this.model.hasPathData(),
                path:this.model.getEncodedPath()
            };

            this.$el.html(nodeTemplate(data));

            this.input = this.$('input.load-3D').first();
            this.checkbox = this.$('.selectable-part-checkbox');

	        //If the ComponentViews is checked
	        if(this.options.checkedAtInit){
		        App.instancesManager.loadComponent(this.model);
	        }

            if (this.options.isLast) {
                this.$el.addClass('last');
            }

            this.onAllResultPathAdded();

            return this;
        },

        onComponentSelected: function (e) {
            e.stopPropagation();
            this.$('>a').trigger('component:selected', [this.model, this.$el]);

        },

	    onEditPart: function () {
		    var model = new Part({partKey: this.model.getNumber() + '-' + this.model.getVersion()});
            var iteration = this.model.getIteration();

		    model.fetch().success(function () {
			    new PartModalView({
				    model: model,
                    iteration: iteration,
                    productId: App.config.productId,
                    productConfigSpec: ['wip','latest','latest-released'].indexOf(App.config.productConfigSpec)===-1 ? App.config.productConfigSpec : null
			    }).show();
		    });

	    },

        openPathDataModal : function () {
            Backbone.Events.trigger('path-data:clicked',this.model);
        }
    });

    ComponentViews.Assembly = Backbone.View.extend({

        tagName: 'li',

        className: 'expandable',

        events: {
            'click a:first': 'onComponentSelected',
            'click .openModal:first': 'onEditPart',
            'change input.load-3D:first': 'onLoad3D',
            'change input.selectable-part-checkbox:first': 'selectPart',
            'click .hitarea:first': 'onToggleExpand',
            'click > .fa-asterisk': 'openPathDataModal'
        },

        initialize: function () {
            this.isExpanded = false;
            _.bindAll(this, ['onLoad3D']);
            this.listenTo(this.options.resultPathCollection, 'reset', this.onAllResultPathAdded);
            this.$el.attr('id', 'path_' + this.model.getEncodedPath());
            this.isForbidden = this.model.isForbidden();
            this.isLock = this.model.isCheckout() && this.model.isLastIteration(this.model.get('iteration')) && !this.model.isCheckoutByConnectedUser();
        },

        onAllResultPathAdded: function () {
            var isInResultPaths = this.options.resultPathCollection.contains(this.model.attributes.partUsageLinkId);
            this.$el.toggleClass('resultPath',isInResultPaths);
        },

        selectPart:function(e){
            this.$el.trigger('checkbox:selected', [e.target.checked,this.model]);
            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        openPathDataModal : function () {
            Backbone.Events.trigger('path-data:clicked',this.model);
        },
        onLoad3D: function (event) {
            if (event) {
                if (event.target.checked) {
                    App.instancesManager.loadComponent(this.model);
                }
                else {
                    App.instancesManager.unLoadComponent(this.model);
                }
            }
        },

        render: function () {

            var data = {
                isNode:true,
                number: this.model.attributes.number,
                name: this.model.attributes.name,
                version: this.model.getVersion(),
                iteration: this.model.getIteration(),
                isLastIteration: this.model.isLastIteration( this.model.getIteration()),
                amount: this.model.getAmount(),
                unit: this.model.getUnit(),
                checkedAtInit: this.options.checkedAtInit,
                isForbidden: this.isForbidden,
                isCheckoutByAnotherUser: this.model.isCheckout() && !this.model.isCheckoutByConnectedUser(),
                isCheckoutByConnectedUser: this.model.isCheckout() && this.model.isCheckoutByConnectedUser(),
                hasUnreadModificationNotifications: this.model.hasUnreadModificationNotifications(),
                isReleased: this.model.isReleased(),
                isObsolete: this.model.isObsolete(),
                isLock: this.isLock,
                partUsageLinkReferenceDescription: this.model.getPartUsageLinkReferenceDescription(),
                isSubstitute: this.model.isSubstitute(),
                isOptional:this.model.isOptional(),
                hasSubstitutes : this.model.hasSubstitutes(),
                hasPathData:this.model.hasPathData(),
                path:this.model.getEncodedPath()
            };

            this.$el.html(nodeTemplate(data));

            this.input = this.$('input.load-3D').first();
            this.checkbox = this.$('.selectable-part-checkbox');

	        //If the ComponentViews is checked
	        if(this.options.checkedAtInit && (!App.collaborativeView || !App.collaborativeView.roomKey)){
		        App.instancesManager.loadComponent(this.model);
	        }

            if (data.isForbidden || data.isLock) {
                this.$el.removeClass('expandable');
            }

            if (this.options.isLast) {
                if (data.isForbidden || data.isLock) {
                    this.$el.addClass('last');
                } else {
                    this.$el.addClass('lastExpandable')
                        .children('.hitarea')
                        .addClass('lastExpandable-hitarea');
                }
            }

            this.onAllResultPathAdded();


            if (_.contains(this.expandedViews, this.model.attributes.number)) {
                this.onToggleExpand();
            }

            return this;
        },

        onToggleExpand: function () {
            if (!this.hasChildrenNodes()) {
                new ComponentViews.Components({
                    collection: this.model.children,
                    parentView: this.$el,
                    parentChecked: this.isChecked(),
                    resultPathCollection: this.options.resultPathCollection
                });
            }
            this.toggleExpand();
        },

        toggleExpand: function () {
            if (!this.isForbidden && !this.isLock) {
                this.$el.toggleClass('expandable collapsable')
                    .children('.hitarea')
                    .toggleClass('expandable-hitarea collapsable-hitarea');

                if (this.options.isLast) {
                    this.$el.toggleClass('lastExpandable lastCollapsable')
                        .children('.hitarea')
                        .toggleClass('lastExpandable-hitarea lastCollapsable-hitarea');
                }

                this.isExpanded = !this.isExpanded;

                var childrenNode = this.$('>ul');

                if (this.isExpanded) {
                    childrenNode.show();
                    expandedViews.push(this.model.attributes.number);
                } else {
                    childrenNode.hide();
                    expandedViews = _(expandedViews).without(this.model.attributes.number);
                }
            }
        },

        hasChildrenNodes: function () {
            var childrenNode = this.$('>ul');
            return childrenNode.length > 0;
        },

        onComponentSelected: function (e) {
            e.stopPropagation();
            this.$('>a').trigger('component:selected', [this.model, this.$el]);
        },

        isChecked: function () {
            return this.input.prop('checked');
        },

	    onEditPart: function () {
		    var model = new Part({partKey: this.model.getNumber() + '-' + this.model.getVersion()});
            var iteration = this.model.getIteration();

		    model.fetch().success(function () {
			    new PartModalView({
				    model: model,
                    iteration: iteration,
                    productId: App.config.productId,
                    productConfigSpec: ['wip','latest','latest-released'].indexOf(App.config.productConfigSpec)===-1 ? App.config.productConfigSpec : null
			    }).show();
		    });

	    }
    });

    return ComponentViews;
});
