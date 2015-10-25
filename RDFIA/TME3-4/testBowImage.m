
pathSav='/Users/floriantoque/Documents/UPMC/M2DAC_git/RDFIA/TME3-4/';
patchmin='/Users/floriantoque/Documents/UPMC/M2DAC_git/RDFIA/TME3-4/patchmin';
nomDico='/Users/floriantoque/Documents/UPMC/M2DAC_git/RDFIA/TME1/KMEANS/dictionnaireVisuel';
pathIm='/Users/floriantoque/Documents/UPMC/M2DAC_git/RDFIA/Scene/';
pathDes='/Users/floriantoque/Documents/UPMC/M2DAC_git/RDFIA/TME1/Sifts/';
load(nomDico);
% Ouverture d'une image aleatoire
[I, nomim, sifts]=randomImageDes(pathIm, pathDes);

% Calcul de matormClusters
matNormClusters=sum(bestCenters.^2,2);

% Calcul de patchmin

load(patchmin);

% Calcul du BoW
[bow,nc]=computeBow(sifts, bestCenters, matNormClusters);
save(strcat(pathSav,'beau'),'bow')

visuBoW(I, patchmin, bow, nc, nomim);