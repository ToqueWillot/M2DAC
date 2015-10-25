addpath('/users/nfs/Etu4/3101314/M2/RDFIA/TME3et4/codesTME3-4/bow/')
bestCenters = '/users/nfs/Etu4/3101314/M2/RDFIA/TME3et4/Descripteurs/dictionnaireVisuel';
pathBaseDir = '/users/nfs/Etu4/3101314/M2/RDFIA/TME3et4/Scene/';
pathBaseDirDes = '/users/nfs/Etu4/3101314/M2/RDFIA/TME3et4/Sifts/';
%[patchmin] = visuDico(bestCenters, pathBaseDir, pathBaseDirDes);


pathTest = '/users/nfs/Etu4/3101314/M2/RDFIA/TME3et4/Bows/bedroom/image_0005';
load(pathTest)

sum(bow)

a=randi(15)