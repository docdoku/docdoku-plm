/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/workspace-dashboard.html',
    'common-objects/models/workspace'
], function (Backbone, Mustache, template, Workspace) {
    'use strict';

    var maxDays = 30 ;

    function calculateDaysSinceTimestamp(timestamp){
        var days = parseInt(((((new Date().getTime() - timestamp)/1000)/60)/60)/24);
        return days < maxDays ? days+1 : maxDays;
    }

    var WorkspaceDashboardView = Backbone.View.extend({

        events: {

        },

        initialize: function () {
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                workspace: _.findWhere(App.config.workspaces.administratedWorkspaces,{id:App.config.workspaceId})
            }));

            Workspace.getDiskUsageStats(App.config.workspaceId)
                .then(this.constructDiskUsageChart.bind(this));

            Workspace.getUsersStats(App.config.workspaceId)
                .then(this.constructUsersCharts.bind(this));

            Workspace.getStatsOverView(App.config.workspaceId)
                .then(this.constructEntitiesChart.bind(this));

            Workspace.getCheckedOutDocumentsStats(App.config.workspaceId)
                .then(this.constructCheckedOutDocsCharts.bind(this));

            Workspace.getCheckedOutPartsStats(App.config.workspaceId)
                .then(this.constructCheckedOutPartsCharts.bind(this));

            return this;
        },

        constructDiskUsageChart:function(diskUsage){

            var diskUsageData = [];
            var totalDiskUsage = 0;

            for(var key in diskUsage){
                if(diskUsage[key]){
                    diskUsageData.push({key:key,y:diskUsage[key],f:bytesToSize(diskUsage[key])});
                    totalDiskUsage+=diskUsage[key];
                }
            }

            if(diskUsageData.length === 0){
                diskUsageData.push({key:'No Data available',y:100,f:'Nothing to show'})
            }

            this.$("#disk_usage_chart span.total").html(bytesToSize(totalDiskUsage));

            // TODO translate keys

            nv.addGraph(function() {
                var chart;
                var width = 400, height = 200;

                chart = nv.models.pieChart()
                    .x(function(d) { return d.key })
                    .y(function(d) { return d.y})
                    .showLabels(false)
                    .values(function(d) { return d })
                    .color(d3.scale.category10().range())
                    .width(width)
                    .height(height)
                    .donut(false)
                    .tooltipContent(function(x, y, e, graph){return diskUsageTooltip(x, e.point.f)});

                d3.select("#disk_usage_chart svg")
                    .datum([diskUsageData])
                    .transition().duration(1200)
                    .attr('width', width)
                    .attr('height', height)
                    .call(chart);

                return chart;
            });
        },

        constructEntitiesChart:function(entitiesCount){

            var entitiesData = [];

            var usersCount = entitiesCount.users;
            var documentsCount = entitiesCount.documents;
            var productsCount = entitiesCount.products;
            var partsCount = entitiesCount.parts;

            // TODO translate
            entitiesData.push({key:"Users",y:usersCount});
            entitiesData.push({key:"Documents",y:documentsCount});
            entitiesData.push({key:"Products",y:productsCount});
            entitiesData.push({key:"Parts",y:partsCount});

            nv.addGraph(function() {
                var width = 400, height = 200;
                var chart = nv.models.discreteBarChart()
                    .x(function(d) { return d.key })
                    .y(function(d) { return d.y })
                    .staggerLabels(true)
                    .tooltips(false)
                    .width(width)
                    .height(height)
                    .showValues(true);

                chart.yAxis.tickFormat(d3.format('.f'));

                d3.select('#entities_chart svg')
                    .datum([{key:"entities",values:entitiesData}])
                    .transition().duration(500)
                    .call(chart);

                return chart;
            });
        },

        constructCheckedOutDocsCharts:function(cod){



            var codData = [];
            var totalCod = 0;
            for(var user in cod){
                var documents = cod[user];
                var mapDayDoc = {};
                var userData = {
                    key: user,
                    values: []
                };
                for(var i = 0 ; i < maxDays+1 ; i++){
                    mapDayDoc[i]=0;
                }
                for(var i = 0; i < documents.length ; i++){
                    mapDayDoc[calculateDaysSinceTimestamp(documents[i].date)] ++;
                    totalCod++;
                }
                for(var day in mapDayDoc){
                    if(mapDayDoc[day] > 0){
                        userData.values.push({
                            x: day, y: mapDayDoc[day] , size: mapDayDoc[day]
                        });
                    }
                }
                codData.push(userData);
            }

            nv.addGraph(function() {
                var width = 900, height = 290;
                var chart = nv.models.scatterChart()
                    .showDistX(true)
                    .showDistY(true)
                    .width(width)
                    .height(height)
                    .forceX([0,maxDays])
                    .forceY([0,null])
                    .color(d3.scale.category10().range());

                chart.xAxis.tickFormat(d3.format('.f'));
                chart.xAxis.axisLabel(App.config.i18n.CHART_AXIS_DAYS_NUMBER);
                chart.yAxis.tickFormat(d3.format('.f'));
                chart.yAxis.axisLabel(App.config.i18n.CHART_AXIS_DOCUMENTS_NUMBER);

                d3.select('#cod_chart svg')
                    .datum(codData)
                    .transition().duration(500)
                    .call(chart);

                nv.utils.windowResize(chart.update);

                return chart;
            });

            this.$("#cod_chart span.total").html(totalCod);


        },

        constructCheckedOutPartsCharts:function(cop){

            var copData = [];
            var totalCop = 0;
            for(var user in cop){
                var parts = cop[user];
                var mapDayPart = {};
                var userData = {
                    key: user,
                    values: []
                };
                for(var i = 0 ; i < maxDays+1 ; i++){
                    mapDayPart[i]=0;
                }
                for(var i = 0; i < parts.length ; i++){
                    mapDayPart[calculateDaysSinceTimestamp(parts[i].date)] ++;
                    totalCop++;
                }
                for(var day in mapDayPart){
                    if(mapDayPart[day] > 0){
                        userData.values.push({
                            x: day, y: mapDayPart[day] , size: mapDayPart[day]
                        });
                    }
                }
                copData.push(userData);
            }


            nv.addGraph(function() {
                var width = 900, height = 290;
                var chart = nv.models.scatterChart()
                    .showDistX(true)
                    .showDistY(true)
                    .width(width)
                    .height(height)
                    .forceX([0,maxDays])
                    .forceY([0,null])
                    .color(d3.scale.category10().range());

                chart.xAxis.tickFormat(d3.format('.f'));
                chart.xAxis.axisLabel(App.config.i18n.CHART_AXIS_DAYS_NUMBER);
                chart.yAxis.tickFormat(d3.format('.f'));
                chart.yAxis.axisLabel(App.config.i18n.CHART_AXIS_PARTS_NUMBER);

                d3.select('#cop_chart svg')
                    .datum(copData)
                    .transition().duration(500)
                    .call(chart);

                nv.utils.windowResize(chart.update);

                return chart;
            });
            this.$("#cop_chart span.total").html(totalCop);
        },

        constructUsersCharts:function(usersInWorkspace, usersStats){

            var usersAndGroupData = [];

            // TODO translate keys
            for(var key in usersStats){
                usersAndGroupData.push({key:key,y:usersStats[key]});
            }

            nv.addGraph(function() {
                var width = 800, height = 200;
                var chart = nv.models.discreteBarChart()
                    .x(function(d) { return d.key })
                    .y(function(d) { return d.y })
                    .staggerLabels(true)
                    .tooltips(false)
                    .width(width)
                    .height(height)
                    .showValues(true);

                chart.yAxis.tickFormat(d3.format('.f'));

                d3.select('#users_chart svg')
                    .datum([{key:"entities",values:usersAndGroupData}])
                    .transition().duration(500)
                    .call(chart);

                return chart;
            });

        }
    });

    return WorkspaceDashboardView;
});
