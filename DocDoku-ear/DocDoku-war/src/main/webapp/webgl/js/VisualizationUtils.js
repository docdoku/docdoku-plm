function VisualizationUtils(){};

VisualizationUtils.xAxisNormalized = new THREE.Vector3(1,0,0).normalize();
VisualizationUtils.yAxisNormalized = new THREE.Vector3(0,1,0).normalize();
VisualizationUtils.zAxisNormalized = new THREE.Vector3(0,0,1).normalize();

VisualizationUtils.rotateAroundOneWorldAxis = function(object, axis, radians) {
    var rotWorldMatrix = new THREE.Matrix4();
    rotWorldMatrix.makeRotationAxis(axis.normalize(), radians);
    rotWorldMatrix.multiplySelf(object.matrix);
    object.matrix = rotWorldMatrix;
    object.rotation.getRotationFromMatrix(object.matrix, object.scale);
}

VisualizationUtils.rotateAroundWorldAxis= function(object, rx, ry, rz) {

    var rotWorldMatrixX = new THREE.Matrix4();
    rotWorldMatrixX.makeRotationAxis(VisualizationUtils.xAxisNormalized, rx);

    var rotWorldMatrixY = new THREE.Matrix4();
    rotWorldMatrixY.makeRotationAxis(VisualizationUtils.yAxisNormalized, ry);

    var rotWorldMatrixZ = new THREE.Matrix4();
    rotWorldMatrixZ.makeRotationAxis(VisualizationUtils.zAxisNormalized, rz);

    object.matrix = rotWorldMatrixZ.multiplySelf(rotWorldMatrixY).multiplySelf(rotWorldMatrixX);
    object.rotation.getRotationFromMatrix(object.matrix, object.scale);

}