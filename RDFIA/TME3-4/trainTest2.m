function [values, w, b]=trainTest2(train, test, y)
    % apprentissage du classifieur svm
    svm = svmtrain(y, train, '-c 1000 -t linear');
    [w, b] = getPrimalSVMParameters(svm);
    % On calcul le score des donn�es de test sur le svm appris
    values = test * w + b;
end
