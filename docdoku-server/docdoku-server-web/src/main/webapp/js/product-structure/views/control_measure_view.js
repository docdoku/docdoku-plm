/*global sceneManager*/
define(
    [
        "text!templates/control_measure.html",
        "i18n!localization/nls/product-structure-strings"
    ],
    function(template,i18n){

    var MeasureOptionsView = Backbone.View.extend({

        template : Mustache.compile(template),

        className:"side_control_group",

        events:{
        },

        initialize:function(){
            _.bindAll(this);
            this.state = false;
            this.points=[];
            this.line=null;
            var that = this ;
            sceneManager.setMeasureListener(function(point){
                that.onSceneClick(point);
            });
            this.geometry = new THREE.SphereGeometry(10, 10, 10);
            this.material = new THREE.LineBasicMaterial({
                color: 0xff0000
            });

        },

        render:function(){
            this.$el.html(this.template({i18n:i18n}));
            this.bindDomElements();
            this.$switch.bootstrapSwitch();
            this.$switch.bootstrapSwitch('setState', this.state);
            this.$switch.on('switch-change',this.switchMeasureState);
            return this;
        },

        bindDomElements:function(){
            this.$switch = this.$(".measure-switch");
            this.$result = this.$("#measure-result");
        },

        createMeasurePointMesh:function(point){
            var sphere = new THREE.Mesh(this.geometry,this.material);
            sphere.overdraw = true;
            sphere.position.set(point.x,point.y,point.z);
            sceneManager.scene.add(sphere);
            return sphere;
        },

        drawLine:function(){

            var material = new THREE.LineBasicMaterial({
                color: 0xff0000,
                opacity: 1,
                linewidth: 1
            });

            var geometry = new THREE.Geometry();
            geometry.vertices=[
                new THREE.Vector3(this.points[0].point.x,this.points[0].point.y,this.points[0].point.z),
                new THREE.Vector3(this.points[1].point.x,this.points[1].point.y,this.points[1].point.z)
            ];
            sceneManager.scene.remove(this.line);
            this.line = new THREE.Line(geometry,material);
            this.line.overdraw = true;
            sceneManager.scene.add(this.line);
        },

        showResult:function(){
            var distance = Math.round(this.lineDistance()*100)/100 + " mm";
            this.$result.html(distance);
        },

        onSceneClick:function(point){
            if(point === -1){
                this.clear();
                return;
            }

            if(!this.points[0]){
                this.setFirstPoint(point);
            }else if(!this.points[1]){
                this.setSecondPoint(point);
                this.drawLine();
                this.showResult();
            }else{
                this.clear();
                this.setFirstPoint(point);
            }
        },

        setFirstPoint:function(point){
            this.points[0] = {
                point : point,
                mesh: this.createMeasurePointMesh(point)
            };
        },
        setSecondPoint:function(point){
            this.points[1] = {
                point : point,
                mesh: this.createMeasurePointMesh(point)
            };
        },

        clear:function(){
            this.$result.html("")
            if(this.points[0] && this.points[0].mesh){
                sceneManager.scene.remove(this.points[0].mesh);
            }
            if(this.points[1] && this.points[1].mesh){
                sceneManager.scene.remove(this.points[1].mesh);
            }
            if(this.line){
                sceneManager.scene.remove(this.line);
                this.line = null;
            }
            this.points=[];
        },

        switchMeasureState:function (e, data) {
            this.state = data.value;
            if(!this.state){
                this.clear();
            }
            sceneManager.setMeasureState(data.value);
        },

        lineDistance:function(){
            var xs = 0;
            var ys = 0;

            xs = this.points[1].point.x - this.points[0].point.x;
            xs = xs * xs;

            ys = this.points[1].point.y - this.points[0].point.y;
            ys = ys * ys;

            return Math.sqrt( xs + ys );
        }

    });

    return MeasureOptionsView;

});