function [confusionMatrix, nGoodCat] = multiClassPrediction(predictclassifieurs, imCatTest)
    %nb categories
    nbCat = size(imCatTest,1);
    
    nGoodCat = zeros(1,nbCat);
    
    %create the confusion matrix
    confusionMatrix = zeros(nbCat, nbCat);
    
    [predMax, argmax] = max(predictclassifieurs);
    binrange = 1:15; 
    begin = 1; 
    for i=1:nbCat
        argMaxCat = argmax(1,begin:(begin+imCatTest(i))-1);
        confusionMatrix(i,:) = (histc(argMaxCat, binrange)/sum(histc(argMaxCat, binrange)/100));
        nGoodCat(i) = confusionMatrix(i,i);
        begin = begin + imCatTest(i);
    end

end