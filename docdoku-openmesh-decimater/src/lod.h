#ifndef LOD_H
#define LOD_H
#include <OpenMesh/Core/IO/MeshIO.hh>
#include <OpenMesh/Core/Mesh/TriMesh_ArrayKernelT.hh>
#include <OpenMesh/Tools/Decimater/DecimaterT.hh>
#include <OpenMesh/Tools/Decimater/ModQuadricT.hh>
using namespace std;
class LOD
{

typedef OpenMesh::TriMesh_ArrayKernelT<> MyMesh;
public:
    LOD(MyMesh mesh,std::string filename,std::string outputPath,float percent);

    void writeMesh();
    void decimateMesh();
    void generateLOD();
    float computeMeshVolume();
    void setVolume(float volume);
    void setVolumeDiff(float oldV,float newV);
    float getVolume();
    float getVolumeDiff();

    void setPercent(float percent);
    float getPercent();
private:
    std::string _filename;
    std::string _outputPath;
    float _percent;
    MyMesh _mesh;
    float _volume;
    float _volumeDiff;
};

#endif // LOD_H
