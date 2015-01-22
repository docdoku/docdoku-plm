#-------------------------------------------------
#
# Project created by QtCreator 2014-06-17T15:46:44
#
#-------------------------------------------------

QT -= core gui


TARGET = OpenMeshDecimater
CONFIG   += console
CONFIG   -= app_bundle
CONFIG+=c++11
#QMAKE_MACOSX_DEPLOYMENT_TARGET = 10.9
##QMAKE_CXXFLAGS+=  -std=c++11 #-stdlib=libc++ -mmacosx-version-min=10.7
INCLUDEPATH += $$PWD/
HEADERS += \
    lod.h

SOURCES += \
    main.cpp \
    lod.cpp



unix: LIBS += -L/usr/local/lib/OpenMesh/ -lOpenMeshCore
unix: LIBS += -L/usr/local/lib/OpenMesh/ -lOpenMeshTools

INCLUDEPATH += /usr/local/include/
DEPENDPATH += /usr/local/include/
