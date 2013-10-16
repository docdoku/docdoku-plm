function VisualizationUtils(){};

VisualizationUtils.xAxisNormalized = new THREE.Vector3(1,0,0).normalize();
VisualizationUtils.yAxisNormalized = new THREE.Vector3(0,1,0).normalize();
VisualizationUtils.zAxisNormalized = new THREE.Vector3(0,0,1).normalize();

VisualizationUtils.rotWorldMatrixX = new THREE.Matrix4();
VisualizationUtils.rotWorldMatrixY = new THREE.Matrix4();
VisualizationUtils.rotWorldMatrixZ = new THREE.Matrix4();

VisualizationUtils.rotateAroundOneWorldAxis = function(object, axis, radians) {
    var rotWorldMatrix = new THREE.Matrix4();
    rotWorldMatrix.makeRotationAxis(axis.normalize(), radians);
    rotWorldMatrix.multiply(object.matrix);
    object.matrix = rotWorldMatrix;
    object.rotation.getRotationFromMatrix(object.matrix, object.scale);
}

VisualizationUtils.rotateAroundWorldAxis= function(object, rx, ry, rz) {

    VisualizationUtils.rotWorldMatrixX.makeRotationAxis(VisualizationUtils.xAxisNormalized, rx);
    VisualizationUtils.rotWorldMatrixY.makeRotationAxis(VisualizationUtils.yAxisNormalized, ry);
    VisualizationUtils.rotWorldMatrixZ.makeRotationAxis(VisualizationUtils.zAxisNormalized, rz);

    object.matrix = VisualizationUtils.rotWorldMatrixZ.multiply(VisualizationUtils.rotWorldMatrixY).multiply(VisualizationUtils.rotWorldMatrixX);
    object.rotation.setFromRotationMatrix(object.matrix, "XYZ");

}