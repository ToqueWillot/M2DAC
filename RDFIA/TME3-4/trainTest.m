function [values]=trainTest(train, test, y)
    % apprentissage du classifieur svm
    svm = svmtrain(y, train, '-c 1000 -t linear');
    [w, b] = getPrimalSVMParameters(svm);
    % On calcul le score des données de test sur le svm fit
    values = test * w + b;
end
