#include "lod.h"

#include <OpenMesh/Core/Mesh/TriMesh_ArrayKernelT.hh>
#include <OpenMesh/Core/Mesh/Handles.hh>

template<class T>
inline T SignedVolumeOfTriangle(const vector<T>& p1,const vector<T>& p2,const vector<T>& p3) {
    float v321 = p3[0]*p2[1]*p1[2];
    float v231 = p2[0]*p3[1]*p1[2];
    float v312 = p3[0]*p1[1]*p2[2];
    float v132 = p1[0]*p3[1]*p2[2];
    float v213 = p2[0]*p1[1]*p3[2];
    float v123 = p1[0]*p2[1]*p3[2];
    return (1.0f/6.0f)*(-v321 + v231 + v312 - v132 - v213 + v123);
}
LOD::LOD(MyMesh mesh,std::string filename,std::string outputPath,float percent)
{
    this->_mesh=mesh;
    this->_filename=filename;
    this->_outputPath=outputPath;
    this->_percent=percent;
}

void LOD::decimateMesh(){
    typedef OpenMesh::Decimater::DecimaterT< MyMesh > Decimater;
    typedef OpenMesh::Decimater::ModQuadricT< MyMesh >::Handle HModQuadric;

    Decimater decimater(_mesh); // a decimater object, connected to a mesh
    HModQuadric hModQuadric; // use a quadric module

    decimater.add(hModQuadric); // register module at the decimater
    decimater.module( hModQuadric ).unset_max_err();
    decimater.initialize(); // let the decimater initialize the mesh and the

    float vertD = roundf(_percent*decimater.mesh().n_vertices());
    decimater.decimate_to(vertD);
    std::cout << _filename <<" decimated to "<<int(_percent*100)<<"% (" <<int(vertD)<<"/"<<decimater.mesh().n_vertices()<<")"<<std::endl;

    //CLEAN MESH from removed edges
    int i=0;
    for (MyMesh::FaceIter f_it= _mesh.faces_begin(); f_it!= _mesh.faces_end(); ++f_it){

        if (_mesh.valence(f_it.handle())==0){
            _mesh.delete_face(f_it.handle(),false);
            i++;
        }
    }
    _mesh.garbage_collection();

    std::cout << "Faces deleted " << i++ << std::endl;
}

void LOD::writeMesh(){

    int b = _filename.find_last_of("/");
    int e = _filename.find_last_of(".");
    std::string file=_filename.substr(b,e-b);

    std::string lod=to_string(int(_percent*100));

    std::string outputFilename= _outputPath+file+lod+".obj";
    if (!OpenMesh::IO::write_mesh(_mesh, outputFilename))
    {
        std::cerr << "write error\n";
        exit(1);
    }
    cout << "Output file created : "<< outputFilename<<endl;
}

float LOD::computeMeshVolume(){

    float sum=0;
    for (MyMesh::FaceIter f_it=_mesh.faces_begin(); f_it!=_mesh.faces_end(); ++f_it){

        std::vector< std::vector<float> > face ;
        int i=0;

        for (MyMesh::FaceVertexIter fv_it = _mesh.fv_begin(f_it.handle()); fv_it; ++fv_it)
        {
            MyMesh::Point point=_mesh.point(fv_it);
            face.push_back({point[0],point[1],point[2]});
            i++;
        }
        sum+= SignedVolumeOfTriangle(face[0],face[1],face[2]);
    }
    return sum;
}
void LOD::setVolume(float volume){
    this->_volume=volume;
}
void LOD::setVolumeDiff(float oldV,float newV){
    this->_volumeDiff=oldV-newV;
}
float LOD::getVolume(){
    return this->_volume;
}
float LOD::getVolumeDiff(){
    return this->_volumeDiff;
}

void LOD::generateLOD(){
    //setVolume(computeMeshVolume());
    //float oldV = getVolume();
    decimateMesh();
    //setVolume(computeMeshVolume());
    //float newV=getVolume();
    //setVolumeDiff(oldV,newV);
    //cout <<getVolumeDiff()<<endl;
    writeMesh();
}




void LOD::setPercent(float percent){
    this->_percent=percent;
}

float LOD::getPercent(){
    return this->_percent;
};
