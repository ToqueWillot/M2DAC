T=1; % Le nombre de fois ou on renouvelle le test
pathBow='/Users/floriantoque/Documents/UPMC/M2DAC_git/RDFIA/TME3-4/Bows/';

K=1000;
nTrain=100;
catinit=1;
catend=15;
scoreT=zeros(1,T);
for t=1:T
    % Chargement des donnees d'apprentissage et de test    
    [imCat,imCatTest]=NbImCatAllTest(pathBow, nTrain);
    
    [train, test]=loadData(nTrain, imCat, pathBow, K);

    predictclassifieurs = zeros(catend, size(test,1));
    % Apprentissage du classifieur
    for index=catinit:catend
        [y, ytest]=labelsTrainTest(nTrain,size(test,1),imCat,index);
        y
        [values] = trainTest(train, test, y);
        predictclassifieurs(index,:) = values;
    end
    
    [matConf, txCat] = multiClassPrediction(predictclassifieurs, imCatTest);
    
    scoreT(t) = mean(txCat);
end

figure();
subplot(2,2,1);
plot(txCat);
subplot(2,2,2);
imagesc(matConf);

moyenne = mean(scoreT)
ecartType = std(scoreT)
