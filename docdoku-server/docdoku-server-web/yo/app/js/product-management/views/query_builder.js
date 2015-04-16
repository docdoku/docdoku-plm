/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/query_builder.html',
    'query-builder',
    'selectize',
    '../../utils/query-builder-options',
    'common-objects/views/alert',
    'collections/configuration_items'
], function (Backbone, Mustache, template, queryBuilder, selectize,querybuilderOptions, AlertView, ConfigurationItemCollection) {
    'use strict';
    var QueryBuilderModal = Backbone.View.extend({

        events: {
            'click .search-button': 'onSearch',
            'change select.query-list':'onSelectQueryChange',
            'click .delete-selected-query':'deleteSelectedQuery',
            'click .reset-button' : 'onReset',
            'click .clear-select-badge': 'onClearSelect',
            'click .clear-where-badge': 'onClearWhere',
            'click .clear-order-by-badge': 'onClearOrderBy',
            'click .clear-group-by-badge': 'onClearGroupBy',
            'click .clear-context-badge' : 'onClearContext'
        },

        delimiter: '/',

        initialize: function () {

            this.selectizeAvailableOptions = querybuilderOptions.fields;

            this.queryBuilderFilters = querybuilderOptions.filters;

            this.selectizeOptions = {
                plugins: ['remove_button','drag_drop', 'optgroup_columns'],
                persist: true,
                delimiter:this.delimiter,
                optgroupField: 'group',
                optgroupLabelField: 'name',
                optgroupValueField: 'id',
                optgroups: querybuilderOptions.groups,

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
            var contextSelectize = this.$context[0].selectize;

            selectSelectize.clear();
            orderBySelectize.clear(true);
            orderBySelectize.clearOptions();
            groupBySelectize.clear(true);
            groupBySelectize.clearOptions();
            contextSelectize.clear();

            this.$deleteQueryButton.hide();
        },

        onSelectQueryChange:function(e){

            this.clear();

            var selectSelectize = this.$select[0].selectize;
            var orderBySelectize = this.$orderBy[0].selectize;
            var groupBySelectize = this.$groupBy[0].selectize;
            var contextSelectize = this.$context[0].selectize;

            if(e.target.value){
                var query = _.findWhere(this.queries,{id: parseInt(e.target.value,10)});
                this.$where.queryBuilder('setRules', query.queryRule);

                _.each(query.selects,function(value){
                    selectSelectize.addItem(value);
                });

                _.each(query.orderByList,function(value){
                    orderBySelectize.addItem(value, true);
                });

                _.each(query.groupedByList,function(value){
                    groupBySelectize.addItem(value, true);
                });

                _.each(query.productsId, function(value){
                    contextSelectize.addItem(value, true);
                });

            }else{
                this.$where.queryBuilder('reset');
            }
            this.$deleteQueryButton.toggle(e.target.value != '');
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
                            success: function (data) {
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

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.bindDomElements();

            var self = this;
            var url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/attributes';
            $.ajax({
                type: 'GET',
                url: url,
                success: function (data) {
                    _.each(data, function(attribute){

                        var attributeType = querybuilderOptions.types[attribute.type];
                        self.queryBuilderFilters.push({
                            id: 'attr-'+attribute.type+'.'+attribute.name,
                            label: attribute.name,
                            type: attributeType,
                            optgroup: _.findWhere(querybuilderOptions.groups, {id : 'attr-'+attribute.type}).name
                        });

                        self.selectizeAvailableOptions.push({
                            name:attribute.name,
                            value:'attr-'+attribute.type+'.'+attribute.name,
                            group:'attr-'+attribute.type
                        });

                    });

                    self.fillSelectizes();
                    self.fetchQueries();
                    self.initWhere();
                },
                error: function (errorMessage) {
                    self.$('#alerts').append(new AlertView({
                        type: 'warning',
                        message: App.config.i18n.QUERY_ATTRIBUTES_ERROR
                    }).render().$el);

                    self.fillSelectizes();
                    self.fetchQueries();
                    self.initWhere();
                }
            });

            this.$saveSwitch.bootstrapSwitch();
            this.$saveSwitch.bootstrapSwitch('setState', false);
            this.$inputName.toggle(false);
            var self = this;
            this.$saveSwitch.on('switch-change', function (event, state) {
                self.$inputName.toggle(state.value);
            });

            return this;
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
            var contextOption = _.clone(this.selectizeOptions);
            contextOption.maxItems = 1;
            this.$context.selectize(contextOption);

            var self = this;
            var productCollection = new ConfigurationItemCollection();
            productCollection.fetch().success(function(productsList){
                _.each(productsList, function(product){
                    self.$context[0].selectize.addOption({
                        name:product.id,
                        value:product.id
                    });
                });
            }).error(function(){
                self.$('#alerts').append(new AlertView({
                    type: 'warning',
                    message: App.config.i18n.QUERY_CONTEXT_ERROR
                }).render().$el);
            });

            this.$select.selectize(this.selectizeOptions);
            this.$select[0].selectize.addOption(this.selectizeAvailableOptions);

            var self = this;
            this.$select[0].selectize.on('item_add', function(value, $item){
                var data = _.findWhere(self.$select[0].selectize.options, {value: value});

                self.$groupBy[0].selectize.addOption(data);
                self.$groupBy[0].selectize.refreshOptions(false);

                self.$orderBy[0].selectize.addOption(data);
                self.$orderBy[0].selectize.refreshOptions(false);
            });

            this.$select[0].selectize.on('item_remove', function(value, $item){
                self.$groupBy[0].selectize.removeOption(value);
                self.$groupBy[0].selectize.refreshOptions(false);

                self.$orderBy[0].selectize.removeOption(value);
                self.$orderBy[0].selectize.refreshOptions(false);
            });


            this.$orderBy.selectize(this.selectizeOptions);
            this.$groupBy.selectize(this.selectizeOptions);
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
        },

        onReset: function(){
            this.clear();
            this.$where.queryBuilder('reset');
            this.$saveSwitch.bootstrapSwitch('setState', false);
            this.$selectQuery.val('');
        },

        onSearch:function(){
            var self = this;

            var isValid = this.$where.queryBuilder('validate');

            if(isValid) {

                var context = this.$context[0].selectize.getValue().split(this.delimiter);
                var selectList = this.$select[0].selectize.getValue().split(this.delimiter);
                var where = this.$where.queryBuilder('getRules');
                var orderByList = this.$orderBy[0].selectize.getValue().split(this.delimiter);
                var groupByList = this.$groupBy[0].selectize.getValue().split(this.delimiter);

                var saveQuery = this.$saveSwitch.bootstrapSwitch('status');

                if (saveQuery && this.$inputName.val().length === 0) {
                    this.$inputName.focus();
                    return;
                }

                var queryData = {
                    productsId:context,
                    selects: selectList,
                    orderByList: orderByList,
                    groupedByList: groupByList,
                    queryRule: where,
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



    return QueryBuilderModal
});
