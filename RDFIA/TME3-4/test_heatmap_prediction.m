% Tous les paths necessaires

pathBow='/Users/floriantoque/Documents/UPMC/M2DAC_git/RDFIA/TME3-4/Bows/';% la ou on a sauvegarder les bows
pathIm='/Users/floriantoque/Documents/UPMC/M2DAC_git/RDFIA/Scene/';
pathClassifier='/Users/floriantoque/Documents/UPMC/M2DAC_git/RDFIA/TME3-4/Classifiers/'
pathDes='/Users/floriantoque/Documents/UPMC/M2DAC_git/RDFIA/TME1/Sifts/'

patchmin='/Users/floriantoque/Documents/UPMC/M2DAC_git/RDFIA/TME3-4/patchmin';
nomDico='/Users/floriantoque/Documents/UPMC/M2DAC_git/RDFIA/TME1/KMEANS/dictionnaireVisuel';


%definition des paramètres d'apprentissage
nTrain = 100;
K = 1000;
catinit=1;
catend=15;

% Recuperation du nb d'images pour chaque catégorie
[imCat,imCatTest]=NbImCatAllTest(pathBow, nTrain);

% chargement des données de train et de test
[ train , test , ras ] = loadData2( nTrain , imCat , pathBow, K);

%predictclassifieurs = zeros(catend, size(test,1));
%{
% Entrainement des classifieurs pour chaque catégorie
for index=catinit:catend
    [y, ytest]=labelsTrainTest(nTrain,size(test,1),imCat,index);
    [values, w, b] = trainTest2(train, test, y);
    %predictclassifieurs(index,:) = values;
    save(strcat(pathClassifier,'classifier_',num2str(index),'_w.mat'),'w');
    save(strcat(pathClassifier,'classifier_',num2str(index),'_b.mat'),'b');
end
%}
%chargement du dictionnaire visuel et du matchmin
load(nomDico);
load(patchmin);

% Chargement d'une image aléatoire
[ I, nomim, sifts, index] = randomImageDes2( pathIm, pathDes );

% Calcul du bow pour l'image chargée aléatoirement
matNormClusters=sum(bestCenters.^2,2);
[bow, nc] = computeBow( sifts, bestCenters, matNormClusters );

% Chargement du classifier de la catégorie correspondant à l'image
load(strcat(pathClassifier,'classifier_',num2str(index),'_w'));
[ hmap ] = compute_prediction_heatmap( I, nc, w );

visuHeatMap( I , nc, w, hmap, patchmin, nomim);

