#include <OpenMesh/Core/IO/MeshIO.hh>
#include <OpenMesh/Core/Mesh/TriMesh_ArrayKernelT.hh>
#include <OpenMesh/Tools/Decimater/DecimaterT.hh>
#include <OpenMesh/Tools/Decimater/ModQuadricT.hh>
#include <OpenMesh/Core/Mesh/Handles.hh>

#include <vector>
#include <thread>
#include <math.h>
#include <sys/stat.h>
#include <lod.h>

using namespace std;
#define  maxInstances 500,
#define  maxAngle 0.78539816339
#define  maxDist 100000,
#define  minProjectedSize 0.000001//100,
#define  distanceRating 0.6//0.7,
#define  angleRating 0.4//0.6,//0.5,
#define  volRating 1.0//0.7

// Mesh type
typedef OpenMesh::TriMesh_ArrayKernelT<> MyMesh;

MyMesh loadMesh(std::string filename){

    MyMesh mesh; // a mesh object
    OpenMesh::IO::Options opt;
    if (!OpenMesh::IO::read_mesh(mesh, filename,opt))
    {
        std::cerr << "read error\n";
        exit(1);
    }

    return mesh;
}

void createLOD(std::string filename,std::string outputPath,float percent){

    MyMesh mesh=loadMesh(filename);
    //float sum1 = computeMeshVolume(&mesh);
    LOD l= LOD(mesh,filename,outputPath,percent);
    l.generateLOD();


}
void goThread(LOD * lod){
    lod->generateLOD();
}

void launchDecimation(std::vector<LOD*>* myLODs){
    bool threadMe=true;

    if (threadMe){

        std::vector<thread> threads ;

        for (int i =0;i<myLODs->size();i++){
            threads.push_back(thread(goThread,myLODs->at(i)));
        }

        for_each(threads.begin(), threads.end(), std::mem_fn(&thread::join));

    }else{

        for (int i =0;i<myLODs->size();i++){
            myLODs->at(i)->generateLOD();
        }

    }

}

int main(int argc, char *argv[]){

    if ( argc < 5 ){
        if (argc == 2 && string(argv[1])=="-h"){
            cout<<"usage: ."<< argv[0] <<" -i <inputFile> -o <outputPath> [OPTIONS]\n";
            cout<<"usage: and <outputPath> must exist before execution of the program\n\n";
            cout << "OPTIONS (choose one among these):\n";
            cout <<"\t<N> ...  : Describe discrete LOD : Where 0.0 < N < 1.0 decimate input mesh down to N%.\n\t\tExample : ."<< argv[0] <<" -i <inputFile> -o <outputPath> 0.1 0.02 0.5 \n ";
            cout <<"\t-e <nbLOD>  : Generate LOD with exponential function : Where <nbLOD> > 1.\n\t\tExample : ."<< argv[0] <<" -i <inputFile> -o <outputPath> -e 5 \n ";
            cout <<"\t-q <nbLOD>  : Generate LOD with quadratic function : Where <nbLOD> > 1.\n\t\tExample : ."<< argv[0] <<" -i <inputFile> -o <outputPath> -q 3 \n ";
        }else{
            cout << "BAD ARGUMENTS"<<endl;
            cout<<"usage: ."<< argv[0] <<" -i <inputFile> -o <outputPath> [OPTIONS]\n";
            cout<<"usage: and <outputPath> must exist before execution of the program\n\n";
            cout << "OPTIONS (choose one among these):\n";
            cout <<"\t<N> ...  : Describe discrete LOD : Where 0.0 < N < 1.0 decimate input mesh down to N%.\n\t\tExample : ."<< argv[0] <<" -i <inputFile> -o <outputPath> 0.1 0.02 0.5 \n ";
            cout <<"\t-e <nbLOD>  : Generate LOD with exponential function : Where <nbLOD> > 1.\n\t\tExample : ."<< argv[0] <<" -i <inputFile> -o <outputPath> -e 5 \n ";
            cout <<"\t-q <nbLOD>  : Generate LOD with quadratic function : Where <nbLOD> > 1.\n\t\tExample : ."<< argv[0] <<" -i <inputFile> -o <outputPath> -q 3 \n ";
        }
    }else{
        std::string filename ;
        std::string outputPath;
        std::vector<bool> occupied(argc, false);
        bool autoGenerationActivated = false;
        bool useQuadratic=true;
        int nbLevel=0;
        occupied.at(0) =true;
        for (int i=1;i<argc;i++){

            if (strcmp(argv[i], "-i")==0){
                filename=argv[i+1];
                occupied.at(i) =true;
                occupied.at(i+1) =true;
             }else if (strcmp(argv[i], "-o")==0){
                outputPath=argv[i+1];
                occupied.at(i) =true;
                occupied.at(i+1) =true;
            }else if (strcmp(argv[i], "-e")==0){
                useQuadratic=false;
                autoGenerationActivated=true;
                occupied.at(i) =true;
                occupied.at(i+1) =true;
                nbLevel = atoi(argv[i+1]);
            }else if (strcmp(argv[i], "-q")==0){
                useQuadratic=true;
                autoGenerationActivated=true;
                occupied.at(i) =true;
                occupied.at(i+1) =true;
                nbLevel = atoi(argv[i+1]);
            }

        }
        struct stat buf;
        if( stat(filename.c_str(), &buf) != 0)
            cout<<"File "+filename+" does'nt exist.\n";
        else{

            MyMesh mesh=loadMesh(filename);
            vector<LOD *> myLODs;
            vector<float> percents;

            if (autoGenerationActivated){

                for (int i=1;i<=nbLevel;i++){
                    float e=0.0;
                    if (useQuadratic){
                        /**
                         * use quadratric function
                         * 1-t^2
                         */
                        cout <<"use quadratic function" <<endl;
                        e=1-pow(float(i)/(nbLevel+1),2);
                    }else{
                        /**
                         * use exponential function
                         * exp(-t^2)
                         */

                        cout <<"use exponential function" <<endl;
                        /* we decide  limit->oo exp(-t^2)=0 start value is i=2 */
                        float limit = 2.0;
                        e=exp(-pow(float(i)*limit/(nbLevel+1),2)); /*2 is */
                    }

                    percents.push_back(e);
                }
                for(vector<float>::iterator i = percents.begin(); i != percents.end();++i){
                        cout <<"% "<<*i <<endl;
                        myLODs.push_back(new LOD(mesh,filename,outputPath,*i));
                }
            }else{

                for (int i=0; i<argc;i++){
                    if (!occupied.at(i)){
                        float percent=atof(argv[i]);
                        myLODs.push_back(new LOD(mesh,filename,outputPath,percent));
                    }
                }
            }
            launchDecimation(&myLODs);

            for(vector<LOD *>::iterator iLod = myLODs.begin(); iLod != myLODs.end();++iLod){
                delete *iLod;
            }
        }
    }

    return 0;
}
