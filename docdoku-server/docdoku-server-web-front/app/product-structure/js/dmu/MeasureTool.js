/*global define,App,THREE*/
define(function(){

    'use strict';

    var MeasureTool = function(callbacks){

        var material = new THREE.LineBasicMaterial({
            color: 0xf47922
        });
        var geometry = new THREE.Geometry();

        this.line = new THREE.Line(geometry, material);
        this.points = [null, null];
        this.callbacks = callbacks;
    };

    MeasureTool.prototype.onClick = function(point){
        if(this.points[0] === null) {
            this.setFirstPoint(point);
        }else if(this.points[1]=== null) {
            this.setSecondPoint(point);
        }
    };

    MeasureTool.prototype.setFirstPoint = function(point){
        this.line.geometry.vertices[0] = point;
        this.line.geometry.vertices[1] = point;
        this.line.geometry.verticesNeedUpdate = true;
        this.points[0] = point.clone();
        this.callbacks.onFirstPoint();
    };

    MeasureTool.prototype.setVirtualPoint = function(point){
        this.line.geometry.vertices[1] = point;
        this.line.geometry.verticesNeedUpdate = true;
    };

    MeasureTool.prototype.setSecondPoint = function(point){
        this.line.geometry.vertices[1] = point;
        this.points[1] = point.clone();
        this.line.geometry.verticesNeedUpdate = true;
        App.sceneManager.drawMeasure(this.points);
        this.callbacks.onSecondPoint();

        this.clear();
    };

    MeasureTool.prototype.clear = function(){
        this.points = [null, null];
    };

    MeasureTool.prototype.hasOnlyFirstPoint = function(){
        return this.points[0] && !this.points[1];
    };

    return MeasureTool;
});
