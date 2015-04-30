/*global _,$,bootbox,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/query_builder.html',
    'selectize',
    '../../utils/query-builder-options',
    'common-objects/views/alert',
    'collections/configuration_items',
    'common-objects/collections/product_instances'
], function (Backbone, Mustache, template,  selectize, queryBuilderOptions, AlertView, ConfigurationItemCollection, ProductInstances) {
    'use strict';
    var QueryBuilderView = Backbone.View.extend({

        events: {
            'click .search-button': 'onSearch',
            'change select.query-list':'onSelectQueryChange',
            'click .delete-selected-query':'deleteSelectedQuery',
            'click .reset-button' : 'onReset',
            'click .clear-select-badge': 'onClearSelect',
            'click .clear-where-badge': 'onClearWhere',
            'click .clear-order-by-badge': 'onClearOrderBy',
            'click .clear-group-by-badge': 'onClearGroupBy',
            'click .clear-context-badge' : 'onClearContext',
            'click .export-excel-button': 'onExport'
        },

        delimiter: ',',

        initialize: function () {

            this.selectizeAvailableOptions = _.clone(queryBuilderOptions.fields);

            this.queryBuilderFilters =  _.clone(queryBuilderOptions.filters);

            this.selectizeOptions = {
                plugins: ['remove_button','drag_drop', 'optgroup_columns'],
                persist: true,
                delimiter:this.delimiter,
                optgroupField: 'group',
                optgroupLabelField: 'name',
                optgroupValueField: 'id',
                optgroups: _.clone(queryBuilderOptions.groups),

                valueField: 'value',
                searchField: ['name'],
                options: null,
                render: {
                    item: function(item, escape) {
                        return '<div><span class="name">' + escape(item.name) + '</span></div>';
                    },
                    option: function(item, escape) {
                        return '<div><span class="label">' + escape(item.name) + '</span></div>';
                    }
                }
            };

            this.fetchAttributes();
            this.fetchTags();
        },

        fetchQueries:function(){

            this.queries = [];
            var queries = this.queries;

            var url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/queries';

            var $select = this.$selectQuery;
            $select.empty();
            $select.append('<option value=""></option>');

            var fillOption = function(q){
                queries.push(q);
                $select.append('<option value="'+ q.id+'">'+ q.name+'</option>');
            };

            $.getJSON(url,function(data){
                data.map(fillOption);
            });

        },

        clear:function(){
            var selectSelectize = this.$select[0].selectize;
            var orderBySelectize = this.$orderBy[0].selectize;
            var groupBySelectize = this.$groupBy[0].selectize;

            selectSelectize.clear();
            orderBySelectize.clear(true);
            orderBySelectize.clearOptions();
            groupBySelectize.clear(true);
            groupBySelectize.clearOptions();

            this.onClearContext();

            this.$deleteQueryButton.hide();
            this.$exportQueryButton.hide();
        },

        onSelectQueryChange:function(e){

            this.clear();

            var selectSelectize = this.$select[0].selectize;
            var orderBySelectize = this.$orderBy[0].selectize;
            var groupBySelectize = this.$groupBy[0].selectize;
            var contextSelectize = this.$context[0].selectize;

            if(e.target.value){
                var query = _.findWhere(this.queries,{id: parseInt(e.target.value,10)});
                if (query.queryRule.rules.length === 0){
                    this.$where.queryBuilder('reset');
                }else{
                    this.$where.queryBuilder('setRules', query.queryRule);
                }

                _.each(query.contexts, function(value){
                    if(!value.serialNumber){
                        contextSelectize.addItem(value.configurationItemId, true);
                    } else {
                        contextSelectize.addItem(value.configurationItemId +'/'+value.serialNumber, true);
                    }
                });

                _.each(query.selects,function(value){
                    selectSelectize.addItem(value);
                });

                _.each(query.orderByList,function(value){
                    orderBySelectize.addItem(value, true);
                });

                _.each(query.groupedByList,function(value){
                    groupBySelectize.addItem(value, true);
                });

            }else{
                this.$where.queryBuilder('reset');
            }
            this.$deleteQueryButton.toggle(e.target.value !== '');
            this.$exportQueryButton.toggle(e.target.value !== '');
        },

        deleteSelectedQuery:function(){
            var self = this;
            var id = this.$selectQuery.val();

            if(id){
                bootbox.confirm(App.config.i18n.DELETE_QUERY_QUESTION, function (result) {
                    if (result) {

                        var url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/queries/'+id;
                        $.ajax({
                            type: 'DELETE',
                            url: url,
                            success: function () {
                                self.clear();
                                self.$where.queryBuilder('reset');
                                self.fetchQueries();
                            },
                            error: function (errorMessage) {
                                self.$('#alerts').append(new AlertView({
                                    type: 'error',
                                    message: errorMessage.responseText
                                }).render().$el);
                            }
                        });
                    }
                });
            }
        },

        fetchAttributes : function(){
            var self = this;
            var url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/attributes';

            $.ajax({
                type: 'GET',
                url: url,
                success: function (data) {
                    _.each(data, function(attribute){

                        var attributeType = queryBuilderOptions.types[attribute.type];
                        var filter = {
                            id: 'attr-'+attribute.type+'.'+attribute.name,
                            label: attribute.name,
                            type: attributeType,
                            realType: attributeType,
                            optgroup: _.findWhere(queryBuilderOptions.groups, {id : 'attr-'+attribute.type}).name
                        };
                        if(attributeType === 'date'){
                            filter.operators = queryBuilderOptions.dateOperators;
                        } else if (attributeType === 'string'){
                            filter.operators = queryBuilderOptions.stringOperators;
                        } else if (attributeType === 'lov'){
                            filter.type = 'string';
                            filter.operators = queryBuilderOptions.lovOperators;
                            filter.input = 'select';
                            var values = [];
                            var index = 0;
                            _.each(attribute.lovItems, function(item){
                                var value = {};
                                value[index] = item.name;
                                values.push(value);
                                index ++;
                            });
                            filter.values = values;
                        } else if(attributeType === 'boolean'){
                            filter.type = 'boolean';
                            filter.operators = queryBuilderOptions.booleanOperators;
                            filter.input = 'select';
                            filter.values = [
                                {'true' : App.config.i18n.TRUE},
                                {'false' : App.config.i18n.FALSE}
                            ];
                        } else if(attributeType === 'double'){
                            filter.operators = queryBuilderOptions.numberOperators;
                        }

                        self.queryBuilderFilters.push(filter);

                        self.selectizeAvailableOptions.push({
                            name:attribute.name,
                            value:'attr-'+attribute.type+'.'+attribute.name,
                            group:'attr-'+attribute.type
                        });

                    });
                },
                error: function () {

                }
            });
        },

        fetchTags : function(){
            var self = this;
            var url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/tags';

            $.ajax({
                type: 'GET',
                url: url,
                success: function (tags) {

                    var filter = {
                        id: 'pr.tags',
                        label: 'TAG',
                        type: 'string',
                        realType: 'string',
                        optgroup: _.findWhere(queryBuilderOptions.groups, {id : 'pr'}).name
                    };

                    var values = [];
                    _.each(tags, function(tag){
                        var value = {};
                        value[tag.id] = tag.label;
                        values.push(value);
                    });

                    filter.operators = queryBuilderOptions.tagOperators;
                    filter.input = 'select';
                    filter.values = values;
                    self.queryBuilderFilters.push(filter);
                },
                error: function () {

                }
            });
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.bindDomElements();
            this.fillSelectizes();
            this.fetchQueries();
            this.initWhere();

            this.$saveSwitch.bootstrapSwitch();
            this.$saveSwitch.bootstrapSwitch('setState', false);
            this.$inputName.toggle(false);
            var $inputName = this.$inputName;
            this.$saveSwitch.on('switch-change', function (event, state) {
                $inputName.toggle(state.value);
            });

            return this;
        },

        destroy:function(){
            this.$where.queryBuilder('destroy');
        },

        initWhere:function(){
            this.$where.queryBuilder({
                filters: this.queryBuilderFilters,
                icons:{
                    add_group : 'fa fa-plus-circle',
                    remove_group : 'fa fa-times-circle',
                    error : 'fa fa-exclamation',
                    remove_rule : 'fa fa-remove',
                    add_rule : 'fa fa-plus'
                }
            });
        },

        fillSelectizes: function(){
            var self = this;

            this.fillContext();

            this.$select.selectize(this.selectizeOptions);
            this.$select[0].selectize.addOption(this.selectizeAvailableOptions);

            this.$select[0].selectize.on('item_add', function(value){
                var data = _.findWhere(self.$select[0].selectize.options, {value: value});

                self.$groupBy[0].selectize.addOption(data);
                self.$groupBy[0].selectize.refreshOptions(false);

                self.$orderBy[0].selectize.addOption(data);
                self.$orderBy[0].selectize.refreshOptions(false);
            });

            this.$select[0].selectize.on('item_remove', function(value){
                self.$groupBy[0].selectize.removeOption(value);
                self.$groupBy[0].selectize.refreshOptions(false);

                self.$orderBy[0].selectize.removeOption(value);
                self.$orderBy[0].selectize.refreshOptions(false);
            });

            this.$orderBy.selectize(this.selectizeOptions);
            this.$groupBy.selectize(this.selectizeOptions);
        },

        fillContext: function(){
            var contextOption = _.clone(this.selectizeOptions);
            contextOption.group = [{id: 'pi', name: App.config.i18n.QUERY_GROUP_PRODUCT}];
            this.$context.selectize(contextOption);

            var self = this;
            var contextSelectize = this.$context[0].selectize;

            var productCollection = new ConfigurationItemCollection();
            productCollection.fetch().success(function(productsList){
                _.each(productsList, function(product){
                    self.$context[0].selectize.addOption({
                        name:product.id,
                        value:product.id,
                        group:'pi'
                    });
                });
            }).error(function(){
                self.$('#alerts').append(new AlertView({
                    type: 'warning',
                    message: App.config.i18n.QUERY_CONTEXT_ERROR
                }).render().$el);
            });

            contextSelectize.on('item_add', function(value){
                self.$select[0].selectize.addOption(queryBuilderOptions.contextFields);
                self.$select[0].selectize.refreshOptions(false);

                if(contextSelectize.items.length === 1) {
                    _.each(queryBuilderOptions.contextFields, function(field){
                        self.selectizeAvailableOptions.push(field);
                    });
                }
            });

            contextSelectize.on('item_remove', function(value){

                if(contextSelectize.items.length === 0) {
                    _.each(queryBuilderOptions.contextFields, function (field) {
                        self.$select[0].selectize.removeOption(field.value);
                        self.$select[0].selectize.refreshOptions(false);

                        self.$groupBy[0].selectize.removeOption(field.value);
                        self.$groupBy[0].selectize.refreshOptions(false);

                        self.$orderBy[0].selectize.removeOption(field.value);
                        self.$orderBy[0].selectize.refreshOptions(false);
                    });
                }
            });


            new ProductInstances().fetch().success(function(productInstances){
                _.each(productInstances, function(pi){
                    contextSelectize.addOptionGroup(pi.configurationItemId,{name: pi.configurationItemId});

                    contextSelectize.addOption({
                        name:pi.serialNumber,
                        value:pi.configurationItemId +'/'+pi.serialNumber,
                        group:pi.configurationItemId
                    });

                });
            });

        },

        bindDomElements: function () {
            this.$modal = this.$('#query-builder-modal');
            this.$where = this.$('#where');
            this.$select = this.$('#select');
            this.$orderBy = this.$('#orderBy');
            this.$groupBy = this.$('#groupBy');
            this.$saveSwitch = this.$('.saveSwitch.switch');
            this.$inputName = this.$('.queryName');
            this.$selectQuery = this.$('select.query-list');
            this.$deleteQueryButton = this.$('.delete-selected-query');
            this.$exportQueryButton = this.$('.export-excel-button');
            this.$searchButton = this.$('.search-button');
            this.$context = this.$('#context');
        },

        onClearSelect: function(){
            var selectSelectize = this.$select[0].selectize;
            var orderBySelectize = this.$orderBy[0].selectize;
            var groupBySelectize = this.$groupBy[0].selectize;

            selectSelectize.clear();
            orderBySelectize.clear(true);
            orderBySelectize.clearOptions();
            groupBySelectize.clear(true);
            groupBySelectize.clearOptions();
        },

        onClearWhere: function(){
            this.$where.queryBuilder('reset');
        },

        onClearOrderBy: function(){
            var orderBySelectize = this.$orderBy[0].selectize;
            orderBySelectize.clear();
        },

        onClearGroupBy: function(){
            var groupBySelectize = this.$groupBy[0].selectize;
            groupBySelectize.clear();
        },

        onClearContext:function(){
            var contextSelectize = this.$context[0].selectize;
            contextSelectize.clear();

            var self = this;
            _.each(queryBuilderOptions.contextFields, function (field) {
                self.$select[0].selectize.removeOption(field.value);
                self.$select[0].selectize.refreshOptions(false);

                self.$groupBy[0].selectize.removeOption(field.value);
                self.$groupBy[0].selectize.refreshOptions(false);

                self.$orderBy[0].selectize.removeOption(field.value);
                self.$orderBy[0].selectize.refreshOptions(false);
            });
        },

        onReset: function(){
            this.clear();
            this.$where.queryBuilder('reset');
            this.$saveSwitch.bootstrapSwitch('setState', false);
            this.$selectQuery.val('');
        },

        onExport: function(){
            var queryId = this.$selectQuery.val();
            var query = _.findWhere(this.queries, {id : parseInt(queryId)});
            var url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/queries/'+query.id+'/format/XLS';
            var link=document.createElement('a');
            link.href = url;
            link.click();
        },

        onSearch:function(){
            var self = this;

            var isValid = this.$where.queryBuilder('validate');
            var rules = this.$where.queryBuilder('getRules');

            if(isValid || (!rules.condition && !rules.rules)) {

                var context = this.$context[0].selectize.getValue().length ? this.$context[0].selectize.getValue().split(this.delimiter) : [];

                var contextToSend = [];
                _.each(context, function(ctx){
                    var productAndSerial = ctx.split('/');
                    contextToSend.push({
                        configurationItemId:productAndSerial[0],
                        serialNumber:productAndSerial[1]
                    });
                });

                var selectList = this.$select[0].selectize.getValue().length ? this.$select[0].selectize.getValue().split(this.delimiter) : [];
                var orderByList = this.$orderBy[0].selectize.getValue().length ? this.$orderBy[0].selectize.getValue().split(this.delimiter) : [];
                var groupByList = this.$groupBy[0].selectize.getValue().length ? this.$groupBy[0].selectize.getValue().split(this.delimiter) : [];

                var saveQuery = this.$saveSwitch.bootstrapSwitch('status');

                if (saveQuery && this.$inputName.val().length === 0) {
                    this.$inputName.focus();
                    return;
                }

                var queryData = {
                    contexts:contextToSend,
                    selects: selectList,
                    orderByList: orderByList,
                    groupedByList: groupByList,
                    queryRule: rules,
                    name: saveQuery ? this.$inputName.val() : ''
                };

                this.$searchButton.button('loading');

                var url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/queries?save=' + saveQuery;

                $.ajax({
                    type: 'POST',
                    url: url,
                    data: JSON.stringify(queryData),
                    contentType: 'application/json',
                    success: function (data) {
                        var dataToTransmit = {
                            queryFilters : self.queryBuilderFilters,
                            queryData:queryData,
                            queryResponse:data,
                            queryColumnNameMapping:self.selectizeAvailableOptions
                        };
                        self.$searchButton.button('reset');
                        self.fetchQueries();
                        self.trigger('query:search', dataToTransmit);
                    },
                    error: function (errorMessage) {
                        self.$searchButton.button('reset');
                        self.$('#alerts').append(new AlertView({
                            type: 'error',
                            message: errorMessage.responseText
                        }).render().$el);
                    }
                });
            }
        }
    });



    return QueryBuilderView;
});
