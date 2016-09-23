/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/part/part_grouped_by_list.html',
    'views/part/part_grouped_by_list_item'
], function (Backbone, Mustache, template, PartGroupedByListItemView){
    'use strict';
    var PartGroupedByView = Backbone.View.extend({

        events: {

        },

        initialize: function () {
            this.items = this.options.data ? this.options.data.queryResponse : [];
            this.selects =  this.options.data ? this.options.data.queryData.selects : [];
            this.orderByList =  this.options.data ? this.options.data.queryData.orderByList : [];
            this.groupedByList =  this.options.data ? this.options.data.queryData.groupedByList : [];
            this.columnNameMapping =  this.options.data ? this.options.data.queryColumnNameMapping: [];
            this.queryFilters =  this.options.data ? this.options.data.queryFilters : [];
        },

        render: function () {
            var self = this;

            var itemsGroupBy = this.groupBy();

            var groups = {};
            var i = 0;

            _.each(_.keys(itemsGroupBy).sort(), function(key){
                groups[''+i] = {
                    key:''+i,
                    name: key === 'undefined' ? null : key,
                    items:itemsGroupBy[key]
                };
                i++;
            });

            var columns = [];
            _.each(this.selects, function(column){
                var data = _.findWhere(self.columnNameMapping, {value:column});
                columns.push(data);
            });

            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                columns:columns,
                groups: _.values(groups)
            }));

            _.each(_.keys(groups),function(key){
                var values = self.orderBy(groups[key].items);
                _.each(values, function(item){
                    var itemView = new PartGroupedByListItemView({
                        queryFilters: self.queryFilters,
                        item : item,
                        columns:columns
                    }).render();
                    self.$('.items-'+key).append(itemView.el);
                });
            });

            return this;
        },

        groupBy:function(){
            var self = this;

            return !this.groupedByList.length ? {'undefined':this.items} : _.groupBy(this.items,function(item){

                var groupByStringToUse = '';
                _.each(self.groupedByList, function(groupByColumn){
                    groupByStringToUse = groupByStringToUse+' > '+item[groupByColumn];
                });

                var keyTrimmed = groupByStringToUse.substring(3).trimRight();
                if(keyTrimmed[keyTrimmed.length-1]==='>'){
                    keyTrimmed = keyTrimmed.substring(0, keyTrimmed.length-1);
                }

                return keyTrimmed;
            });

        },

        orderBy:function(items){
            var self = this;
            return !self.orderByList.length ? items : items.sort(function(item1, item2){

                var item1String = '';
                var item2String = '';
                _.each(self.orderByList, function(orderByColumn){
                    item1String = item1String+'_'+item1[orderByColumn];
                    item2String = item2String+'_'+item2[orderByColumn];
                });
                item1String = item1String.substring(1);
                item2String = item2String.substring(1);

                return item1String > item2String ? 1 :item1String > item2String ? 0 : -1 ;
            });
        }
    });

    return PartGroupedByView;
});
